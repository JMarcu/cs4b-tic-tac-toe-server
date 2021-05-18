package models.ServerMessage.MessageHandler;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import interfaces.Sender;
import java.io.IOException;
import models.GameServer;
import models.Player;
import models.ServerMessage.Message;
import models.ServerMessage.MessageBody.ConnectionSuccessMessageBody;
import models.ServerMessage.MessageBody.CreateLobbyMessageBody;
import models.ServerMessage.MessageType;
import services.GameServerService;
import services.JWTService;

public class CreateLobbyHandler implements Runnable{
    private CreateLobbyMessageBody msg;
    private Sender sender;

    public CreateLobbyHandler(CreateLobbyMessageBody msg, Sender sender){
        this.msg = msg;
        this.sender = sender;
    }
    
    public void run(){
        Message message = null;
        Player player = null;

        if(JWTService.validate(msg.getJWT())){
            DecodedJWT decodedJwt = JWTService.decode(msg.getJWT());
            player = new Gson().fromJson(decodedJwt.getClaim("player").asString(), Player.class);

            GameServer gameServer = new GameServer(msg.getName());
            GameServerService.getInstance().addGameServer(gameServer);
            GameServerService.getInstance().addPlayer(gameServer.getId(), player, sender);

            ConnectionSuccessMessageBody body = new ConnectionSuccessMessageBody(gameServer.getGameState());
            message = new Message(body, MessageType.CONNECTION_SUCCESS);
        } else{
            System.err.println("Invalid JWT in Connection Request :: " + msg.getJWT());
            message = new Message("", MessageType.CONNECTION_FAILURE);
        }

        try {
            sender.send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}