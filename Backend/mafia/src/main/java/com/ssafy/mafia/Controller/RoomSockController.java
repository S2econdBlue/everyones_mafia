package com.ssafy.mafia.Controller;

import com.ssafy.mafia.Model.RoomProtocol.RoomMessageDto;
import com.ssafy.mafia.Service.RoomSockService;
import com.ssafy.mafia.auth.jwt.TokenProvider;
import com.ssafy.mafia.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomSockController {
    private final SimpMessagingTemplate template;
    private final RoomSockService roomSockService;

    private final TokenProvider tokenProvider;

    private static final Logger log = LoggerFactory.getLogger(RoomSockController.class);

    /*
     *
     * ********************************************** *
     *    여기서 부터는 socket 통신을 위한 api 입니다.
     * ********************************************** *
     *
     * */

    @MessageMapping("/room/{room-seq}")
    public void messageControll(@DestinationVariable("room-seq") int roomSeq, StompHeaderAccessor header, @Payload RoomMessageDto message){
        log.info(message.toString());
        final String dest = "/sub/room/" + roomSeq;
        final String type, token;
        int userSeq;

        try{
            token = header.getNativeHeader("token").get(0).toString();
            userSeq = Integer.parseInt(tokenProvider.getAuthentication(token).getName());
            type = message.getHeader().getType();
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        if(type.equals("join")){
            template.convertAndSend(dest, roomSockService.joinRoom(roomSeq, message.getData()).toString());
            template.convertAndSend(dest, roomSockService.getUserlist(roomSeq).toString());
            return;
        }

        if(type.equals("leave")){
            template.convertAndSend(dest, roomSockService.leaveRoom(roomSeq, message.getData()).toString());
            template.convertAndSend(dest, roomSockService.getUserlist(roomSeq).toString());
            return;
        }

        if(type.equals("chat")){
            template.convertAndSend(dest, roomSockService.chat(message.getData()).toString());
            return;
        }

        if(type.equals("interact")){
            template.convertAndSend(dest, roomSockService.interact(roomSeq, userSeq, message.getData()).toString());
            template.convertAndSend(dest, roomSockService.getUserlist(roomSeq).toString());
            return;
        }

        if(type.equals("start")){
//            template.convertAndSend("/sub/room/" + roomSeq, messageService.(message.getData()));
            return;
        }

        if(type.equals("settings")){
            template.convertAndSend(dest, roomSockService.setting(roomSeq, message.getData()).toString());
            return;
        }

    }
}
