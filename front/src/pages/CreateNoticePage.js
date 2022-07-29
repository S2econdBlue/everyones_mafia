import React, { useState } from "react";
import { Container, styleTextField, styleButton } from "../style.js";
import { TextField, Button } from "@mui/material";
import { useNavigate } from "react-router-dom";

const CreateNoticePage = (props) => {
    const navigate = useNavigate();

    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");

    const resetForm = () => {
        setTitle("");
        setContent("");
    };

    const goNoticeListPage = () => {
        navigate("/board");
    };

    return (
        <Container>
            <h2>공지사항 작성 페이지</h2>

            <label htmlFor="title">제목</label>
            <TextField
                style={styleTextField}
                id="title"
                name="title"
                placeholder="제목"
                value={title}
                onChange={(e) => {
                    setTitle(e.target.value);
                }}
            ></TextField>
            <label htmlFor="content">내용</label>
            <TextField
                style={styleTextField}
                id="content"
                name="content"
                placeholder="내용"
                value={content}
                onChange={(e) => {
                    setContent(e.target.value);
                }}
            ></TextField>

            <span>
                <Button style={styleButton} onClick={resetForm}>
                    초기화
                </Button>
                <Button style={styleButton} onClick={goNoticeListPage}>
                    작성 완료
                </Button>
                <Button style={styleButton} onClick={goNoticeListPage}>
                    목록으로
                </Button>
            </span>
        </Container>
    );
};

export default CreateNoticePage;
