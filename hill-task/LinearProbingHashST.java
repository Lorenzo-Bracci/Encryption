//Lorenzo Bracci
//2019-09-29
//This program implements Hashing with linear probing
public class LinearProbingHashST<Key>
{
private int N; // number of key-value pairs in the table
private int M; // size of linear-probing table
private Key[] keys; // the keys
//private Value[] vals; // the values
public LinearProbingHashST(int cap)
{
  this.M = cap;
keys = (Key[]) new Object[M];
//vals = (Value[]) new Object[M];
}
private int hash(Key key)
{ return (key.hashCode() & 0x7fffffff) % M; }
private void resize(int cap)//creates a new linear probing hash with a different size
{
LinearProbingHashST<Key> t = new LinearProbingHashST<Key>(cap);//check problem
for (int i = 0; i < M; i++)
if (keys[i] != null)
t.put(keys[i]);
keys = t.keys;
M = t.M;
}
public void put(Key key)
{
if (N >= M/2) resize(2*M); //if the number of elements is half the size than double the size of M
int i;
for (i = hash(key); keys[i] != null; i = (i + 1) % M)//loops from the hashing position until where a free spot is found
if (keys[i].equals(key)) { return; }
keys[i] = key;
N++;
}
public boolean contains(Key key){
return get(key) != null;
}
public int get(Key key)
{
for (int i = hash(key); keys[i] != null; i = (i + 1) % M)
if (keys[i].equals(key))
return i;
return null;
}
}
