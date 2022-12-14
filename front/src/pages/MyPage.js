import React, { useEffect, useState } from "react";
import styled from 'styled-components';
import { Container, styleButton, styleModal, styleTextField, styleModalkContainer, checkButton, checkStyleButton, smallButton, middleButton, styleContainer, styleTableContainer } from '../style.js';
import { Modal, Box, Button, TextField, makeStyles, Paper } from '@mui/material';
import { useNavigate } from  "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { getUser } from "../redux/slice/UserSlice";
import { logout } from "../redux/slice/UserSlice";
import axios from 'axios';


const MyPage = (props) => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  
  const userData = useSelector(state => state.user);
  const [confirmed, setConfirmed] = useState(null);
  const [withdrawalOpen, setWithdrawalOpen] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [password, setPassword] = useState('');
  const [passwordCheck, setPasswordCheck] = useState('');
  const [isValidNickname, setIsValidNickname] = useState(false);
  const [nickname, setNickname] = useState('');
  const [modalMessage, setModalMessage] = useState('');
  const token = useSelector(state => state.user.accessToken);
  const oauth = useSelector(state => state.user.oauth);

  useEffect(() => {
    if (modalOpen) {
      setWithdrawalOpen(false)
    }
    if (!withdrawalOpen && !modalOpen && !token) {
      navigate("/");
    }
  })

  const confirmPassword = () => {
    axios({
      method: 'post',
      url: '/api/user/checkPw',
      headers: {
        'Content-Type': 'text/plain',
        Authorization: 'Bearer ' + token,
      },
      data: password,
    })
    .then(response => {
      if (response.data) {
        axios({
          method: 'get',
          url: '/api/user/me',
          headers: {
            Authorization: 'Bearer ' + token,
          }
        })
        .then(response2 => {
          setPassword('');
          dispatch(getUser(response2.data));
          setConfirmed(response.data);
        })
      } else {
        handleModalOpen('?????? ?????????????????????.');
      }
    })
    .catch(error => {
      if (error.response.status === 401) {
        // setWrongInputData(true);
      }
    })
  };

  const checkNickname = () => {
    if (nickname.length === 0) {
      handleModalOpen('????????? ???????????? ??????????????????.');
    } else {
      axios({
        method: 'post',
        url: '/api/user/checkNickname',
        headers: {
          'Content-Type': 'text/plain',
          Authorization: 'Bearer ' + token,
        },
        data: nickname,
      })
      .then(response => {
        // console.log(response.data);
        if (response.data) {
          handleModalOpen('?????? ?????? ?????? ??????????????????.');
        } else {
          setIsValidNickname(true);
          handleModalOpen('?????? ????????? ??????????????????.');
        }
      })
      .catch(error => {
        if (error.response.status === 401) {
          // setWrongInputData(true);
        }
      })
    };
  };
  
  const winningP = () => {
    const total = userData.winCount + userData.loseCount;
    if (total > 0) {
      return userData.winCount / total;
    } else {
      return 0;
    }
  };
  
  const handleWithdrawalOpen = () => {
    setWithdrawalOpen(true);
  };
  const handleWithdrawalClose = () => {
    setWithdrawalOpen(false);
  };
  const doWithdrawal = () => {
    axios({
      method: 'delete',
      url: '/api/user/delete',
      headers: {
        Authorization: 'Bearer ' + token,
      },
      data: null,
    })
    .then(() => {
      handleModalOpen('?????????????????????.');
      dispatch(logout());
    })
    .catch(error => {
      setWithdrawalOpen(false);
      if (error.response.status === 401) {
        // setWrongInputData(true);
      }
    })
  }

  const handleModalOpen = (message) => {
    setModalMessage(message);
    setModalOpen(true);
  };
  const handleModalClose = () => {
    setModalOpen(false);
  };

  const updateUserInfo = () => {
    if (nickname.length > 0 && !isValidNickname) {
      handleModalOpen('????????? ??????????????? ????????????.');
    } else if (!isSamePassword()) {
      handleModalOpen('??????????????? ??????????????????.');
    } else {
      if (!nickname.length && !password.length) {
        handleModalOpen('????????? ????????? ????????????.');
      }
      else {
        if (nickname.length > 0) {
          axios({
            method: 'put',
            url: '/api/user/enrollNickname',
            headers: {
              'Content-Type': 'text/plain',
              Authorization: 'Bearer ' + token,
            },
            data: nickname,
          })
          .then(() => {
            setNickname('');
            handleModalOpen('????????? ?????????????????????.');
            axios({
              method: 'get',
              url: '/api/user/me',
              headers: {
                Authorization: 'Bearer ' + token,
              }
            })
            .then(response => {
              dispatch(getUser(response.data));
            })
          })
          .catch(error => {
            if (error.response.status === 401) {
              // setWrongInputData(true);
            }
          })
        }

        if (password.length > 0) {
          axios({
            method: 'post',
            url: '/api/changePw',
            headers: {
              'Content-Type': 'application/json',
              Authorization: 'Bearer ' + token,
            },
            data: {
              email: userData.email,
              password,
            }
          })
          .then(() => {
            setPassword('');
            setPasswordCheck('');
            handleModalOpen('????????? ?????????????????????.');
          })
          .catch(error => {
            if (error.response.status === 401) {
              // setWrongInputData(true);
            }
          })
        }
      }
    }
  }
  
  const isSamePassword = () => {
    return password === passwordCheck;
  };

  return (
    <>
      <Modal
        open={modalOpen}
        onClose={handleModalClose}
        aria-labelledby="modal-title"
      >
        <Box sx={{ ...styleModal, width: 400, backgroundColor:'rgba(30,30,30,0.9)',  border: '3px solid #999' }}>
          <h2 id="modal-title">{modalMessage}</h2>
          <Button sx={{...styleButton, width:35, position:'relative', left:125, top:10}} onClick={handleModalClose}>??????</Button>
        </Box>
      </Modal>
      
      { (!oauth && !confirmed) ?
        <Container>
          <Container style={styleModalkContainer} component={Paper} >
            <Content>
              <h3 style={{color:"#828282", fontSize:22}} >??????????????? ??????????????????.</h3>
              <TextField style={checkButton} type="password" id="outlined-basic" placeholder="????????????" inputProps={{sx:{color:"#828282", fontSize:20}}} onChange={(e) => {setPassword(e.target.value)}} />
            </Content>
            <Content style={{width:300}}>
              <Button sx ={{...smallButton, position:"relative", right:10}} onClick={confirmPassword}>??????</Button>
              <Button sx={{ ...smallButton}} onClick={()=>navigate('/')}>??????</Button>
            </Content>
          </Container>
          </Container>
      :
        <Container>
          <Container style={{height:40}}></Container>
          <Container style={styleContainer} component={Paper}>
            <h1 style={{color:"#dcdcdc", fontSize:'3rem'}} >MyPage</h1>
            <Content style={{width:500}}>
              <label style={{color:"#dcdcdc", fontSize:20}}  htmlFor="nickname">????????? </label>
              <TextField sx={{...checkButton, position:"relative", left:52}} id="nickname" name="nickname" inputProps={{sx:{color:"#b4b4b4", fontSize:20}}}
              placeholder={userData.nickname} value={nickname} onChange={(e) => {setIsValidNickname(false); setNickname(e.target.value);}}></TextField>
              <Button sx={{...checkStyleButton, position:"relative", left:52}} onClick={() => {checkNickname()}}>????????????</Button>
            </Content>
            <Content>
              <label style={{color:"#dcdcdc", marginBottom:5, fontSize:20, height:20}}  htmlFor="email">????????? </label>
              <p style={{color:"#b4b4b4", fontSize:'1.5em' }}>{userData.email}</p>
              {/* <TextField style={styleTextField} id="email" name="email" placeholder="?????????" onChange={(e) => {setEmail(e.target.value)}}></TextField> */}
              {/* <Button style={styleButton} onClick={() => {checkEmail()}}>????????????</Button> */}
            </Content>

            {/* Oauth?????? ???????????? ???????????? ????????? */}
            {oauth ?
            null :
            <>
              <Content>
              <label style={{color:"#dcdcdc" , fontSize:20, height:20}} htmlFor="password" >???????????? </label>
              <TextField style={checkButton} type="password" id="password" name="password" value={password} 
              placeholder="???????????? ??????" inputProps={{sx:{color:"#b4b4b4", fontSize:20}}}
              onChange={(e) => {setPassword(e.target.value)}}></TextField>
              </Content>
              <Content>
              <label style={{color:"#dcdcdc", fontSize:20, height:20}} htmlFor="password-check">???????????? ?????? </label>
              <TextField style={checkButton} type="password" id="password-check" name="password-check" value={passwordCheck} 
              placeholder="???????????? ??????" inputProps={{sx:{color:"#b4b4b4", fontSize:20}}} onChange={(e) => {setPasswordCheck(e.target.value)}}></TextField>
              </Content>
              {!isSamePassword() ? <span>??????????????? ????????????.</span> : null}
            </>
            }
            <Content>
              <label style={{color:"#b4b4b4", fontSize:20}} >???????????? </label>
              <span style={{color:"#b4b4b4", fontSize:20}} id="signup-date">2022.07.25</span>
            </Content>
            <Content sx={{ marginY : 3, fontSize:20}} >
              <div style={{color:"#b4b4b4", fontSize:20}} id="record">
                <label>?????? </label>
                <span>{userData.winCount}</span>
                <span>??? </span>
                <span>{userData.loseCount}</span>
                <span>??? </span>
                <span>{winningP()}</span> 
                <span>%</span>
              </div>
            </Content>
            <p style={{color:"#B90000", marginBottom:10}}> {userData.redUser ? <span >????????? Red User?????????. ???????????? ?????? ???????????? ????????????.</span> : null} </p>
            
            
            <span>
              <Button style={middleButton} onClick={updateUserInfo}>????????????</Button>
              <Button style={middleButton} onClick={handleWithdrawalOpen}>????????????</Button>
              <Button style={middleButton} onClick={() => navigate('/')}>????????????</Button>
            </span>
            <Modal
              open={withdrawalOpen}
              onClose={handleWithdrawalClose}
              aria-labelledby="withdrawal-modal-title"
            >
              <Box sx={{ ...styleModal, width: 400 }}>
                <h2 id="withdrawal-modal-title">?????? ?????????????????????????</h2>
                <Button sx={{...middleButton, marginBottom:30}} onClick={doWithdrawal}>??????</Button>
                <Button sx={{...middleButton, marginBottom:30}} onClick={handleWithdrawalClose}>??????</Button>
              </Box>
            </Modal>
          </Container>
        </Container>
      }
    </>
  );
};

const Content = styled.div`
  overflow: hidden;
  display: block;
  text-align: center;
`

export default MyPage;