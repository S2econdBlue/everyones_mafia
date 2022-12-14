package com.ssafy.mafia.Controller;

import com.ssafy.mafia.Model.*;
import com.ssafy.mafia.Service.RoomService;
import com.ssafy.mafia.Service.SessionService;
import com.ssafy.mafia.auth.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/room")
@CrossOrigin("*")
@Api(tags = {"로비/대기방 기능"})
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService service;

    private final SessionService sessionService;

    @ApiOperation(value = "방 생성", notes = "방 생성")
    @PostMapping
    public ResponseEntity<SettingsDto> createRoom(@RequestBody SettingsDto request){
        log.info("User ["+SecurityUtil.getCurrentUserId()+"] 가 새로운 방을 생성했습니다.");
        log.info("방 생성 요청 데이터 : " + request.toString());

        int hostUser = SecurityUtil.getCurrentUserId();
        request.getRoomInfo().setHostUser(hostUser);

        // 방 생성
        SettingsDto response = service.createRoom(request.getRoomInfo(), request.getGameInfo());
        service.setHost(response.getRoomInfo().getRoomSeq(), hostUser);
        service.setRoomPassword(response.getRoomInfo().getRoomSeq(), request.getRoomInfo().getPassword());

        // 오픈비두 세션 생성
        sessionService.createSession(response.getRoomInfo().getRoomSeq());

        return new ResponseEntity<SettingsDto>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "방 전체 목록 조회", notes = "방 전체 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<List<RoomInfoResponseDto>> getRoomList(){
        log.info("[Room] 방 전체 목록 조회", service.getAllRooms());
        // 방 목록 조회
        return new ResponseEntity<List<RoomInfoResponseDto>>(service.getAllRooms(), HttpStatus.OK);
    }

    @ApiOperation(value = "필터를 이용한 방 목록 조회", notes = "필터를 이용한 방 목록 조회")
    @PostMapping("/list")
    public ResponseEntity<List<RoomInfoResponseDto>> searchByFilter(@RequestBody RoomSearchFilterDto filter){
        // 방 목록 필터로 검색
        return new ResponseEntity<List<RoomInfoResponseDto>>(service.searchRoomsByFilter(filter), HttpStatus.OK);
    }

    @ApiOperation(value = "방 상세 정보 조회", notes = "방 상세 정보 조회")
    @GetMapping("/{room-seq}/info")
    public ResponseEntity<RoomInfoResponseDto> roomDetailInfo(@PathVariable("room-seq") int roomSeq){
        // 방 상세 정보 조회
        return new ResponseEntity<RoomInfoResponseDto>(service.getRoomInfo(roomSeq), HttpStatus.OK);
    }

    @ApiOperation(value = "방 상세 정보 수정", notes = "방 상세 정보 수정")
    @PutMapping
    public ResponseEntity<RoomInfoResponseDto> modifyRoomInfo(@RequestBody RoomInfoDto roomInfo){
        // Todo : 호스트 유저만 방 정보 수정 가능
        int currentUser = SecurityUtil.getCurrentUserId();
        int hostUser = service.getRoomInfo(roomInfo.getRoomSeq()).getHostUser();

        if(currentUser != hostUser){
            log.error("방 정보 수정에 실패했습니다.");
            log.error("호스트가 아닙니다.");
            return new ResponseEntity<RoomInfoResponseDto>(new RoomInfoResponseDto(), HttpStatus.BAD_REQUEST);
        }

        // 방 상세 정보 수정
        return new ResponseEntity<RoomInfoResponseDto>(service.modifyRoomInfo(roomInfo), HttpStatus.OK);
    }

    // 방 삭제
    @ApiOperation(value = "방 삭제", notes = "방 삭제")
    @DeleteMapping("/{room-seq}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("room-seq") int roomSeq){
        service.deleteRoom(roomSeq);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ApiOperation(value = "방 입장", notes = "방 입장")
    @PostMapping("/{room-seq}/join")
    public ResponseEntity<SettingsDto> joinRoom(@PathVariable("room-seq") int roomSeq){
        int userSeq = SecurityUtil.getCurrentUserId();
        log.info("[Room] 유저({}) 방 ({}) 입장", userSeq, roomSeq);
        // 방 입장
        return new ResponseEntity<SettingsDto>(service.joinRoom(roomSeq, userSeq), HttpStatus.OK);
    }

    @ApiOperation(value = "방 퇴장", notes = "방 퇴장")
    @PostMapping("/{room-seq}/leave")
    public ResponseEntity<Void> leaveRoom(@PathVariable("room-seq") int roomSeq){
        // 방 퇴장
        int userSeq = SecurityUtil.getCurrentUserId();
        service.leaveRoom(roomSeq, userSeq);
        sessionService.leaveSession(roomSeq, userSeq);

        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ApiOperation(value = "호스트 위임", notes = "호스트 위임")
    @PostMapping("/{room-seq}/change-host/{other-user-seq}")
    public ResponseEntity<Void> passHost(@PathVariable("room-seq") int roomSeq, @PathVariable("other-user-seq") int otherUserSeq){
        int thisUser = SecurityUtil.getCurrentUserId();

        // 호스트 권한 넘겨주기
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ApiOperation(value = "오픈비두 토큰 발급", notes = "오픈비두 서버 접속을 위한 토큰 발급")
    @GetMapping("/{room-seq}/video-token")
    public ResponseEntity<String> getToken(@PathVariable("room-seq") int roomSeq){
        int userSeq = SecurityUtil.getCurrentUserId();
        String token = sessionService.joinSession(roomSeq, userSeq);

        if(token == null){
            return new ResponseEntity<String>("토큰 발급 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(token, HttpStatus.OK);
    }

}
