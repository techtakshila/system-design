class Cache {
    private String cacheId;
    private HashFunction hashFunction;

    public Cache(String cacheId, HashFunction hashFunction) {
        this.cacheId = cacheId;
        this.hashFunction = hashFunction;
    }

    public String getCacheId() {
        return cacheId;
    }

    public HashFunction getHashFunction() {
        return hashFunction;
    }

    public void setCacheId(String cacheId) {
        this.cacheId = cacheId;
    }

    public int hashCode(){
        return hashFunction.hashCode(cacheId);
    }
}



class Server<K extends Cache> {
    private String serverId;
    private Map<Integer, K> caches;
    private HashFunction hashFunction;

    public Server(String serverId, HashFunction hashFunction){
        this.serverId = serverId;
        this.hashFunction = hashFunction;
        caches = new HashMap<>();
    }

    public Map<Integer, K> getCaches() {
        return caches;
    }

    public void setCaches(Map<Integer, K> caches) {
        this.caches = caches;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public int hashCode(){
        return hashFunction.hashCode(serverId);
    }
}


class DistributedCachingImp<T extends Server, K extends Cache> {

    private SortedMap<Integer, T> circle = new TreeMap<>();
    private int noOfReplicas;

    public DistributedCachingImp(int noOfReplicas) {
        this.noOfReplicas = noOfReplicas;
    }

    public void add(T node) {
        circle.put(node.hashCode(), node);
    }

    public void remove(T node) {
        circle.remove(node.hashCode());
    }

    public void addCache(K cache) {
        T server = getServer(cache.hashCode());
        addToCache(server, cache);

        for(int i = 0; i < noOfReplicas; i++){
            server = getNextServer(server.hashCode());
            addToCache(server, cache);
        }

    }

    private void addToCache(T server, K cache) {
        Map<Integer, K> caches = server.getCaches();
        caches.put(cache.hashCode(), cache);
    }

    public K getCache(String cacheId, HashFunction hashFunction) {
        T server = getServer(hashFunction.hashCode(cacheId));
        K retrievedCache = getFromCache(server, hashFunction.hashCode(cacheId));

        if(retrievedCache != null) {
            return retrievedCache;
        }

        for(int i = 0; i < noOfReplicas; i++){
            server = getNextServer(server.hashCode());
            K nextServerCache = getFromCache(server, hashFunction.hashCode(cacheId));

            if(nextServerCache != null) {
                return retrievedCache;
            }
        }
        return null;
    }

    private K getFromCache(T server, int hash){
        Map<Integer, K> caches = server.getCaches();
        return caches.get(hash);
    }

    private T getNextServer(int hash) {
        SortedMap<Integer, T> tailMap =
                circle.tailMap(hash);
        hash = tailMap.isEmpty() ?
                circle.firstKey() : tailMap.firstKey();
        return circle.get(hash);
    }

    private T getServer(int hash) {
        if(circle.isEmpty()){
            return null;
        };
        if(!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap =
                    circle.tailMap(hash);
            hash = tailMap.isEmpty() ?
                    circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }
}


public class DistributedCachingSimulation {

    public static void main(String[] args) {
        DistributedCachingImp distributedCachingImp = new DistributedCachingImp(2);
        StringHashFunction hashFunction = new StringHashFunction();

        Server<Cache> server1 = new Server<>("A", hashFunction);
        Server<Cache> server2 = new Server<>("E", hashFunction);
        Server<Cache> server3 = new Server<>("M", hashFunction);
        Server<Cache> server4 = new Server<>("V", hashFunction);

        distributedCachingImp.add(server1);
        distributedCachingImp.add(server2);
        distributedCachingImp.add(server3);
        distributedCachingImp.add(server4);

        Cache cache1 = new Cache("B", hashFunction);
        Cache cache2 = new Cache("G", hashFunction);
        Cache cache3 = new Cache("N", hashFunction);
        Cache cache4 = new Cache("X", hashFunction);

        distributedCachingImp.add(server1);
        distributedCachingImp.add(server2);
        distributedCachingImp.add(server3);
        distributedCachingImp.add(server4);

        distributedCachingImp.addCache(cache1);
        distributedCachingImp.addCache(cache2);
        distributedCachingImp.addCache(cache3);
        distributedCachingImp.addCache(cache4);

        System.out.println(distributedCachingImp.getCache(cache3.getCacheId(), cache3.getHashFunction()).getCacheId());

        distributedCachingImp.remove(server3);

        System.out.println(distributedCachingImp.getCache(cache3.getCacheId(), cache3.getHashFunction()).getCacheId());
        System.out.println(distributedCachingImp.getCache(cache1.getCacheId(), cache1.getHashFunction()).getCacheId());
    }
}

