package com.ssafy.mafia.Repository;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ssafy.mafia.Entity.RoomInfo;
import com.ssafy.mafia.Entity.User;
import com.ssafy.mafia.Model.RoomInfoDto;
import com.ssafy.mafia.Model.RoomManager;
import com.ssafy.mafia.Model.RoomProtocol.RoomDataDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Transactional
public class RoomRepo {

    // db 접근을 위한 entity manager
    @Autowired
    private EntityManager em;

    // 방별 정보를 담고 있는 map
    private final Map<Integer, RoomManager> map = new ConcurrentHashMap<>();

    /*
    *
    * 대기방 관련 기능 구현
    *
    * */

    // 전체 방 리스트 조회
    public List<RoomInfo> getAllRooms(){
        return em.createQuery("select r from RoomInfo r", RoomInfo.class).getResultList();
    }

    // 방 신규 생성
    public RoomInfo createRoom(RoomInfoDto roomInfo){

        // 집어넣을 데이터 설정
        RoomInfo entity = new RoomInfo();
        entity.setTitle(roomInfo.getTitle());
        entity.setCapacity(roomInfo.getCapacity());

        // DB insert
        em.persist(entity);

        // primary key 생성
        em.flush();

        // 방 생성
        map.put(entity.getRoomSeq(), new RoomManager());
        
        // 데이터 리턴
        return entity;
    }

    // 방 비밀번호 변경
    public void setRoomPassword(int roomSeq, String password){
        RoomInfo entity = em.find(RoomInfo.class, roomSeq);
        entity.setPassword(password);
    }

    // 호스트 유저 변경
    public void setHostUser(int roomSeq, int userSeq){
        RoomInfo entity = em.find(RoomInfo.class, roomSeq);
        entity.setHostUser(userSeq);
    }

    public int getHostUser(int roomSeq){
        RoomInfo entity = em.find(RoomInfo.class, roomSeq);
        return entity.getHostUser();
    }

    // 방 정보 수정
    public RoomInfo modifyRoomInfo(RoomInfoDto roomInfo){
        // 기존의 방 entity 불러오기
        RoomInfo entity = em.find(RoomInfo.class, roomInfo.getRoomSeq());

        // 신규 내용으로 update
        entity.setTitle(roomInfo.getTitle());
        entity.setHostUser(roomInfo.getHostUser());
        entity.setCapacity(roomInfo.getCapacity());

        // update 된 정보 return
        return entity;
    }

    // 방 삭제
    public void deleteRoom(int roomSeq){
        map.remove(roomSeq);
        em.remove(em.find(RoomInfo.class, roomSeq));
    }

    // 방 상세정보 조회
    public RoomInfo getRoomInfo(int roomSeq){
        return em.find(RoomInfo.class, roomSeq);
    }

    // 방 입장
    public void joinRoom(int roomSeq, int userSeq){
        map.get(roomSeq).addUser(userSeq);
    }


    // 방 퇴장
    public void leavRoom(int roomSeq, int userSeq){
        map.get(roomSeq).removeUser(userSeq);
    }

    // 방 전체 인원 조회
    public List<Integer> getAllUsersOfRoom(int roomSeq){
        return map.get(roomSeq).getUsers();
    }



    /*
    *
    * ******************************* *
    * 소켓 통신용
    * ******************************* *
    *
    * */


    // 방 정보 업데이트
    public void updateRoom(int roomSeq, JsonArray list){
        this.map.get(roomSeq).setRoomUser(list);
    }


    // 방에서 유저 삭제
    public void deleteUserSock(int roomSeq, RoomDataDto message){
        this.map.get(roomSeq).removeUser(message);
    }

    public void addUserSock(int roomSeq, RoomDataDto message){
        this.map.get(roomSeq).addUser(message);
    }


    public JsonArray getAllUsersOfRoomSock(int roomSeq){
        return this.map.get(roomSeq).getRoomUser();
    }

    // 유저 ready
    public void seat(int roomSeq, int seatNum, int userSeq){
        this.map.get(roomSeq).seat(userSeq, seatNum);
    }

    // 유저 레디 해제
    public void stand(int roomSeq, int seatNum, int userSeq){
        this.map.get(roomSeq).stand(userSeq, seatNum);
    }

    // 남은 좌석 정보 확인
    public int[] getSeatInfo(int roomSeq){
        return this.map.get(roomSeq).getSeatState();
    }

    // 앉은 좌석 수 확인
    public int getSeatCnt(int roomSeq){
        return this.map.get(roomSeq).getSeatCnt();
    }

}
