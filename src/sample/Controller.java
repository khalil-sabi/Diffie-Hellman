package sample;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Controller {
    @FXML
    AnchorPane tab1;
    @FXML
    AnchorPane tab2;
    @FXML
    Tab t1;
    @FXML
    Tab t2;

    @FXML
    Button init;
    @FXML
    TextField text;
    @FXML
    TextField text2;
    @FXML
    Button envoyer;


    Noeud client,serveur,haut1,haut2,milieu1,milieu2,bas;
    TextArea screenClient,screenServer,screenMiddleMan;
    public void initialize(){

    }

    public void prep(Boolean middleman){
        for(Noeud c : Noeud.connections){
            c.close();
        }
        Noeud.rollbackStatics();

        ImageView clientImage = new ImageView(new Image(getClass().getResource("images/pc.png").toExternalForm()));
        clientImage.setLayoutX(11);clientImage.setLayoutY(289);
        clientImage.setRotate(-1);

        ImageView serveurImage = new ImageView(new Image(getClass().getResource("images/pc3.png").toExternalForm()));
        serveurImage.setLayoutX(1035);serveurImage.setLayoutY(289);

        ImageView haut1Image;
        if(middleman){
            haut1Image = new ImageView(new Image(getClass().getResource("images/attackant.png").toExternalForm()));
            haut1Image.setFitHeight(130);
            haut1Image.setFitWidth(110);
            haut1Image.setLayoutX(426);haut1Image.setLayoutY(140);
        }else{
            haut1Image = new ImageView(new Image(getClass().getResource("images/router.png").toExternalForm()));
            haut1Image.setLayoutX(426);haut1Image.setLayoutY(140);
        }


        ImageView haut2Image = new ImageView(new Image(getClass().getResource("images/router.png").toExternalForm()));
        haut2Image.setLayoutX(687);haut2Image.setLayoutY(169);

        ImageView milieu1Image = new ImageView(new Image(getClass().getResource("images/router.png").toExternalForm()));
        milieu1Image.setLayoutX(486);milieu1Image.setLayoutY(310);

        ImageView milieu2Image = new ImageView(new Image(getClass().getResource("images/router.png").toExternalForm()));
        milieu2Image.setLayoutX(639);milieu2Image.setLayoutY(433);

        ImageView basImage = new ImageView(new Image(getClass().getResource("images/router.png").toExternalForm()));
        basImage.setLayoutX(408);basImage.setLayoutY(523);

        screenClient = new TextArea();
        screenClient.setLayoutX(33);
        screenClient.setLayoutY(298);
        screenClient.setPrefWidth(115);
        screenClient.setPrefHeight(68);
        screenClient.setWrapText(true);
        screenClient.setDisable(true);

        screenServer = new TextArea();
        screenServer.setLayoutX(1057);
        screenServer.setLayoutY(300);
        screenServer.setPrefWidth(105);
        screenServer.setPrefHeight(67);
        screenServer.setWrapText(true);
        screenServer.setDisable(true);

        screenMiddleMan = new TextArea();
        screenMiddleMan.setLayoutX(426 + 13);
        screenMiddleMan.setLayoutY(140 + 32);
        screenMiddleMan.setPrefWidth(80);
        screenMiddleMan.setPrefHeight(60);
        screenMiddleMan.setWrapText(true);
        screenMiddleMan.setDisable(true);

        client = new Noeud(false,clientImage);//9000
        serveur = new Noeud(false,serveurImage);//9001
        haut1 = new Noeud(middleman,haut1Image);//9002
        haut2 = new Noeud(false,haut2Image);//9003
        milieu1 = new Noeud(false,milieu1Image);//9004
        milieu2 = new Noeud(false,milieu2Image);//9005
        bas = new Noeud(false,basImage);//9006



        if(middleman){
            tab2.getChildren().add(client.getImage());
            tab2.getChildren().add(serveur.getImage());
            tab2.getChildren().add(haut1.getImage());
            tab2.getChildren().add(haut2.getImage());
            tab2.getChildren().add(milieu1.getImage());
            tab2.getChildren().add(milieu2.getImage());
            tab2.getChildren().add(bas.getImage());

            tab2.getChildren().add(screenClient);
            tab2.getChildren().add(screenServer);
            tab2.getChildren().add(screenMiddleMan);
        }else{
            tab1.getChildren().add(client.getImage());
            tab1.getChildren().add(serveur.getImage());
            tab1.getChildren().add(haut1.getImage());
            tab1.getChildren().add(haut2.getImage());
            tab1.getChildren().add(milieu1.getImage());
            tab1.getChildren().add(milieu2.getImage());
            tab1.getChildren().add(bas.getImage());


            tab1.getChildren().add(screenClient);
            tab1.getChildren().add(screenServer);
        }



        ArrayList<DataWrapper> voisins = new ArrayList<>();
        voisins.add(new DataWrapper( client, haut1, 1 ));
        voisins.add(new DataWrapper( haut1, haut2, 1 ));
        voisins.add(new DataWrapper( haut2, serveur, 12 ));
        voisins.add(new DataWrapper( client, milieu1, 10 ));
        voisins.add(new DataWrapper( milieu1, milieu2, 15 ));
        voisins.add(new DataWrapper( milieu2, serveur, 5 ));
        voisins.add(new DataWrapper( client, bas, 5 ));
        voisins.add(new DataWrapper( bas, milieu2, 10));

        Thread serveServeur = new Thread(() -> { try{ serveur.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        Thread serveClient = new Thread(() -> { try{ client.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        Thread serveHaut1 = new Thread(() -> { try{ haut1.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        Thread serveHaut2 = new Thread(() -> { try{ haut2.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        Thread serveMilieu1 = new Thread(() -> { try{ milieu1.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        Thread serveMilieu2 = new Thread(() -> { try{ milieu2.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        Thread serveBas = new Thread(() -> { try{ bas.lancer(voisins); }catch(Exception e){ e.printStackTrace(); } });
        serveServeur.start();
        serveClient.start();
        serveHaut1.start();
        serveHaut2.start();
        serveMilieu1.start();
        serveMilieu2.start();
        serveBas.start();
    }
    public void buttonTab1(){
        if(t1.isSelected()){
            prep(false);
        }

    }
    public void buttonTab2(){
        if(t2.isSelected()){
            prep(true);
        }
    }
    public void initCon(){
        try {
            client.initEchange(9001);

            List<Vertex> chemin = client.cheminPlusCourt(serveur);
            List<Vertex> cheminInverse = serveur.cheminPlusCourt(client);
            try{
                TimeUnit.MILLISECONDS.sleep(100);
            }catch (Exception e){ }

            BigInteger p =BigInteger.valueOf(23), g = BigInteger.valueOf(5),a=BigInteger.valueOf(client.getSessions().get(9001).get("a")) ,A = g.pow(a.intValue()).mod(p),b = BigInteger.valueOf(serveur.getSessions().get(9000).get("a")),B = g.pow(b.intValue()).mod(p);
            SequentialTransition seq1 = transition(chemin,A.toString(),tab1,screenServer,A.toString());
            SequentialTransition seq2 = transition(cheminInverse,B.toString(),tab1,screenClient,B.toString());

            SequentialTransition seq = new SequentialTransition(seq1,seq2);
            seq.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void ecrire(){
        try {
            String t;
            List<Vertex> chemin = client.cheminPlusCourt(serveur);
            t = client.envoyer(9001,text.getText());
            SequentialTransition seq = transition(chemin,t,tab1,screenServer,text.getText());
            seq.play();

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void initCon2(){
        try {
            client.initEchange(9001);

            List<Vertex> chemin = client.cheminPlusCourt(serveur);
            List<Vertex> cheminInverse = serveur.cheminPlusCourt(client);
            try{
                TimeUnit.MILLISECONDS.sleep(100);
            }catch (Exception e){ }

            BigInteger p =BigInteger.valueOf(23), g = BigInteger.valueOf(5),a=BigInteger.valueOf(client.getSessions().get(9001).get("a")) ,A = g.pow(a.intValue()).mod(p),b = BigInteger.valueOf(serveur.getSessions().get(9000).get("a")),B = g.pow(b.intValue()).mod(p);
            BigInteger attackerKey = g.pow(7).mod(p);

            SequentialTransition seq1 = transition(chemin.subList(0,2),A.toString(),tab2,null,A.toString());
            SequentialTransition seq12 = transition(chemin.subList(1,chemin.size()),attackerKey.toString(),tab2,screenServer,attackerKey.toString());

            SequentialTransition seq2 = transition(cheminInverse.subList(0,cheminInverse.size() - 1),B.toString(),tab2,null,B.toString());
            SequentialTransition seq22 = transition(cheminInverse.subList(cheminInverse.size() - 2,cheminInverse.size()),attackerKey.toString(),tab2,screenClient,attackerKey.toString());


            SequentialTransition seq = new SequentialTransition(seq1,seq12,seq2,seq22);
            seq.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void ecrire2(){
        try {
            String t;
            List<Vertex> chemin = client.cheminPlusCourt(serveur);
            t = client.envoyer(9001,text2.getText());
            if(chemin.get(1).getConnected().getMiddleman() && chemin.get(1).getConnected().getSessions().containsKey(client.getPort())){
                SequentialTransition seq1 = transition(chemin.subList(0,2),t,tab2,screenMiddleMan,text2.getText());
                SequentialTransition seq2 = transition(chemin.subList(1,chemin.size()),shuffle(t),tab2,screenServer,text2.getText());

                SequentialTransition seq = new SequentialTransition(seq1,seq2);
                seq.play();
            }else if(chemin.get(1).getConnected().getMiddleman()){
                SequentialTransition seq1 = transition(chemin.subList(0,2),t,tab2,screenMiddleMan,text2.getText());
                SequentialTransition seq2 = transition(chemin.subList(1,chemin.size()),t,tab2,screenServer,text2.getText());

                SequentialTransition seq = new SequentialTransition(seq1,seq2);
                seq.play();
            }else{
                SequentialTransition seq = transition(chemin,t,tab2,screenServer,text2.getText());
                seq.play();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SequentialTransition transition(List<Vertex> chemin,String t,AnchorPane tab,TextArea textarea,String addedText){
        ImageView from = chemin.get(0).getConnected().getImage();
        Label l = new Label(t);
        l.setLayoutX(from.getLayoutX());
        l.setLayoutY(from.getLayoutY());
        l.setFont(Font.font(20));
        l.setStyle("-fx-font-weight: bold;-fx-text-fill: #818181;");
        l.setPrefSize(80,10);
        l.setWrapText(true);
        l.setOpacity(0.0);
        tab.getChildren().add(l);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(10));
        fadeIn.setNode(l);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        SequentialTransition seq = new SequentialTransition();
        seq.getChildren().add(fadeIn);
        ArrayList<TranslateTransition> tts = new ArrayList<>();


        for(int i =0;i<chemin.size() - 1;i++){
            ImageView to = chemin.get(i+1).getConnected().getImage();

            TranslateTransition translate = new TranslateTransition();
            translate.setDuration(Duration.seconds(1));
            translate.setNode(l);
            translate.setToX(to.getLayoutX() - from.getLayoutX());
            translate.setToY(to.getLayoutY() - from.getLayoutY());
            tts.add(translate);
        }
        for(Transition transition : tts){
            seq.getChildren().add(transition);
        }
        seq.setOnFinished((ActionEvent event2) ->{
            if(textarea!=null){
                textarea.appendText(addedText+"\n");
            }
            tab.getChildren().remove(l);
        });
        return seq;
    }
    private String shuffle(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }
}
