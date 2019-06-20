class LoadBalancingImpl<T> {

    private HashFunction hashFunction;

    public LoadBalancingImpl(HashFunction hashFunction){
        this.hashFunction = hashFunction;
    }

    private SortedMap<Integer, T> circle = new TreeMap<>();


    public void add(T node) {
        circle.put(hashFunction.hashCode(node), node);
    }

    public void remove(T node) {
        circle.remove(hashFunction.hashCode(node));

    }

    public T getServer(T key) {
        if(circle.isEmpty()){
            return null;
        }
        int hash = hashFunction.hashCode(key);
        if(!circle.containsKey(hash)) {
            SortedMap<Integer, T> tailMap =
                    circle.tailMap(hash);
            hash = tailMap.isEmpty() ?
                    circle.firstKey() : tailMap.firstKey();
        }
        return circle.get(hash);
    }
}

 interface HashFunction<T> {

    public int hashCode(T t);
}
 class StringHashFunction implements HashFunction<String>{

    public int hashCode(String s){
        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return hash;
    }
}

public class LoadBalancingSimulation {

    public static void main(String[] args) {

        LoadBalancingImpl<String> loadBalancing = new LoadBalancingImpl<>(new StringHashFunction());
        loadBalancing.add("A");
        loadBalancing.add("D");
        loadBalancing.add("H");
        loadBalancing.add("T");

        System.out.println(loadBalancing.getServer("Z"));
    }
}