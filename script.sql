CREATE TABLE member (
                        username varchar2(50) primary key,      --아이디
                        password varchar2(100) not null,        --비밀번호
                        role varchar2(50) not null              --권한
);

-- select * from member;
-- delete from member;
commit;