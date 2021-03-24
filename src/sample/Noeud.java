package sample;

import javafx.scene.image.ImageView;

import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.security.Key;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class Noeud {
    private ImageView image;
    private Integer port;
    private Boolean middleman;

    private ServerSocket serverSocket;
    private static Integer ports = 9000;
    public static ArrayList<Noeud> connections = new ArrayList<>();
    ArrayList<Vertex> vertexs;
    Vertex moi;

    private HashMap<Integer,HashMap<String,Integer>> sessions;

    public Noeud(Boolean middleman, ImageView image){
        this.image = image;
        this.middleman = middleman;
        this.port = ports++;
        this.connections.add(this);
    }
    public static void rollbackStatics(){
        connections.clear();
        ports = 9000;
    }
    public Boolean getMiddleman(){
        return middleman;
    }

    public Integer getPort() {
        return port;
    }
    public Noeud getObject(int port){
        for(Noeud c: connections){
            if(c.getPort() == port){
                return c;
            }
        }
        return null;
    }

    public HashMap<Integer, HashMap<String, Integer>> getSessions() {
        return sessions;
    }

    public ImageView getImage() {
        return image;
    }

    public void lancer(ArrayList<DataWrapper> listeVoisins) throws Exception {
        this.sessions = new HashMap<>();
        calculChemin(listeVoisins);
        InputStreamReader in = null;
        BufferedReader bf = null;
        String msg = "";
        try{
            while(msg != null){
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();
                in = new InputStreamReader(socket.getInputStream());
                bf = new BufferedReader(in);
                msg = bf.readLine();
                this.traiterMessage(msg);
                serverSocket.close();
            }
        }catch(SocketException e){
            if(bf != null){
                bf.close();
            }
            if(in != null){
                in.close();
            }
        }catch(Exception e){

        }

    }
    public void close() {
        try{
            Socket socket = new Socket("localhost",port);
            PrintWriter pr = new PrintWriter(socket.getOutputStream());
            pr.println("important");
            pr.flush();

            serverSocket.close();
        }catch(IOException e){

        }

    }

    public String envoyer(Integer port, String text) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        if(sessions.containsKey(port) && sessions.get(port).containsKey("K")){
            text = encrypt(text,sessions.get(port).get("K"));
            ecrire(port,encapsuler(port,"text", text));
        }else{
            ecrire(port,encapsuler(port,"text", text));
        }

        return text;
    }
    private String encapsuler(Integer port, String entete,String text){
        return this.port + "---" + port + "---" + entete + "---" + text ;// source destination message
    }
    public void ecrire(Integer port, String text)throws IOException{
        int prochainPort = cheminPlusCourt(getObject(port)).get(1).getConnected().getPort();
        Socket socket = new Socket("localhost",prochainPort);
        PrintWriter pr = new PrintWriter(socket.getOutputStream());
        pr.println(text);
        pr.flush();
    }
    public void initEchange(Integer port) throws IOException{
        BigInteger p =BigInteger.valueOf(23), g = BigInteger.valueOf(5),a,A;
        Random r = new Random();
        a = BigInteger.valueOf(r.nextInt(100));
        A = g.pow(a.intValue()).mod(p);

        storeSession(port,"p",p.intValue());
        storeSession(port,"a",a.intValue());

        ecrire(port,encapsuler(port,"DH1",p+" "+g+" "+A));
    }
    private void traiterMessage(String msg) throws Exception {
        String[] message = msg.split("---");
        int source = Integer.parseInt(message[0]);
        int destination = Integer.parseInt(message[1]);
        String entete = message[2];
        String text = String.join("---", Arrays.copyOfRange(message,3,message.length));
        if(destination == this.getPort()){
            if(entete.equals("text")){
                if(sessions.containsKey(source) && sessions.get(source).containsKey("K")){
                    System.out.println("DH je suis "+this.port+": "+decrypt(text,sessions.get(source).get("K")));
                }else{
                    System.out.println("je suis "+this.port+": "+text);
                }

            }else if(entete.equals("DH1")){
                BigInteger p = BigInteger.valueOf(Integer.parseInt(text.split(" ")[0]));
                BigInteger g = BigInteger.valueOf(Integer.parseInt(text.split(" ")[1]));
                BigInteger B = BigInteger.valueOf(Integer.parseInt(text.split(" ")[2]));
                BigInteger a,A;
                if(g.compareTo(p)<0){
                    Random r = new Random();
                    a = BigInteger.valueOf(r.nextInt(100));
                    BigInteger K = B.pow(a.intValue()).mod(p);
                    storeSession(source,"K",K.intValue());
                    storeSession(source,"a",a.intValue());
                    A = g.pow(a.intValue()).mod(p);
                    ecrire(source,encapsuler(source,"DH2",String.valueOf(A)));
                }

            }else if(entete.equals("DH2")){
                int a = sessions.get(source).get("a");
                int p = sessions.get(source).get("p");
                BigInteger B = BigInteger.valueOf(Integer.parseInt(text));
                BigInteger K = B.pow(a).mod(BigInteger.valueOf(p));
                storeSession(source,"K",K.intValue());

            }
        }else if(middleman){
            if(entete.equals("DH1")){
                BigInteger z = BigInteger.valueOf(7);
                BigInteger p = BigInteger.valueOf(Integer.parseInt(text.split(" ")[0]));
                BigInteger g = BigInteger.valueOf(Integer.parseInt(text.split(" ")[1]));
                BigInteger B = BigInteger.valueOf(Integer.parseInt(text.split(" ")[2]));
                BigInteger Z;
                if(g.compareTo(p)<0){
                    BigInteger K = B.pow(z.intValue()).mod(p);
                    Z = g.pow(z.intValue()).mod(p);
                    storeSession(source,"K-A",K.intValue());
                    storeSession(destination,"p-A",p.intValue());
                    storeSession(destination,"Z-A",Z.intValue());
                    String nouveauMessage = source+"---"+destination+"---"+entete+"---"+p+" "+g+" "+Z;
                    ecrire(destination,nouveauMessage);
                }
            }else if(entete.equals("DH2")){
                BigInteger Z,z = BigInteger.valueOf(7);
                BigInteger B = BigInteger.valueOf(Integer.parseInt(text));
                int p = sessions.get(source).get("p-A");
                Z = BigInteger.valueOf(sessions.get(source).get("Z-A"));

                BigInteger K = B.pow(z.intValue()).mod(BigInteger.valueOf(p));
                storeSession(source,"K-A",K.intValue());

                String nouveauMessage = source+"---"+destination+"---"+entete+"---"+Z;
                ecrire(destination,nouveauMessage);
            }else if(entete.equals("text")){
                if(sessions.containsKey(source) && sessions.containsKey(destination)){
                    int Ks = sessions.get(source).get("K-A");
                    int Kd = sessions.get(destination).get("K-A");
                    String dec = decrypt(text,Ks);
                    System.out.println("Attaquant "+dec);
                    ecrire(destination,source+"---"+destination+"---"+entete+"---"+encrypt(dec,Kd));
                }else{
                    System.out.println("attaquant "+text);
                    ecrire(destination,msg);
                }


            }
        }else{
            ecrire(destination,msg);
        }
    }


    private void storeSession(int port,String nomVariable,int var){
        if(sessions.containsKey(port)){
            sessions.get(port).put(nomVariable,var);
        }else{
            sessions.put(port,new HashMap<>());
            sessions.get(port).put(nomVariable,var);
        }
    }

    private String encrypt(String data,int K) throws NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Key key = new SecretKeySpec(ByteBuffer.allocate(16).putInt(K).array(),"AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] enc = c.doFinal(data.getBytes());
        String encryptedText = Base64.getEncoder().encodeToString(enc);
        return encryptedText;
    }
    private String decrypt(String encryptedData,int K) throws Exception{
        Key key = new SecretKeySpec(ByteBuffer.allocate(16).putInt(K).array(),"AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
        byte[] decValue = c.doFinal(decodedValue);
        String decyptedText = new String(decValue);
        return decyptedText;
    }


    public void calculChemin(ArrayList<DataWrapper> listeVoisins){
        vertexs = new ArrayList<>();
        moi = null;
        for(Noeud c : connections){
            Vertex v = new Vertex(c);
            vertexs.add(v);
            if(c.equals(this)){
                moi = v;
            }
        }
        for(DataWrapper d:listeVoisins){
            Vertex v1 = null,v2=null;
            int poids = d.getPoids();
            for(Vertex v: vertexs){
                if(v.getConnected().equals(d.getVoisin1())){
                    v1 = v;
                }
                if(v.getConnected().equals(d.getVoisin2())){
                    v2 = v;
                }
            }
            v1.adjacents.add(new Voisin(v2,poids));
            v2.adjacents.add(new Voisin(v1,poids));
        }
        moi.minDistance = 0;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(moi);

        while (!vertexQueue.isEmpty()) {
            Vertex u = vertexQueue.poll();

            for (Voisin e : u.adjacents) {
                Vertex v = e.autre;
                int poids = e.poids;
                int distanceThroughU = u.minDistance + poids;
                if (distanceThroughU < v.minDistance) {
                    vertexQueue.remove(v);

                    v.minDistance = distanceThroughU;
                    v.DernierVisite = u;
                    vertexQueue.add(v);
                }
            }
        }

    }

    public List<Vertex> cheminPlusCourt(Noeud t) {
        Vertex autre = null;
        for(Vertex v: vertexs){
            if(v.getConnected().equals((t))){
                autre = v;
            }
        }
        List<Vertex> path = new ArrayList<>();
        for (Vertex vertex = autre; vertex != null; vertex = vertex.DernierVisite){
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }
}