package com.example.demo.src.user;


import com.example.demo.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public GetUserInfoRes selectUserInfo(int userIdx){
        String selectUsersInfoQuery = "SELECT name, nickName, profileImgUrl, introduction, website,\n" +
                "       IF(postCount is null, 0, postCount) as postCount,\n" +
                "       IF(follewerCount is null, 0, follewerCount) as follewerCount,\n" +
                "       IF(follewingCount is null, 0, follewingCount) as follewingCount\n" +
                "FROM User\n" +
                "        left join (SELECT userIdx, COUNT(postIdx) as postCount\n" +
                "                    FROM Post\n" +
                "                    WHERE status = 'ACTIVE'\n" +
                "                    group by userIdx) as p on p.userIdx = User.userIdx\n" +
                "        left join (SELECT followerIdx, COUNT(followIdx) as follewerCount\n" +
                "                FROM Follow\n" +
                "                WHERE status = 'ACTIVE'\n" +
                "                group by followerIdx) f1 on f1.followerIdx = User.userIdx\n" +
                "        left join (SELECT followeeIdx, COUNT(followIdx) as follewingCount\n" +
                "                FROM Follow\n" +
                "                WHERE status = 'ACTIVE'\n" +
                "                group by followeeIdx) f2 on f2.followeeIdx = User.userIdx\n" +
                "WHERE User.userIdx = ?;";

        int selectUserInfoParam = userIdx;

        return this.jdbcTemplate.queryForObject(selectUsersInfoQuery,  // List 형태이면 -> query, List가 아니면 queryForObject
                (rs,rowNum) -> new GetUserInfoRes(
                        rs.getString("nickName"),
                        rs.getString("name"),
                        rs.getString("profileImgUrl"),
                        rs.getString("website"),
                        rs.getString("introduction"),
                        rs.getInt("followerCount"),
                        rs.getInt("followingCount"),
                        rs.getInt("postCount")
                ), selectUserInfoParam);
    }

    public List<GetUserPostsRes> selectUserPosts(int userIdx){
        String selectUserPostsQuery = "SELECT p.postIdx, pi.imgUrl as postImgUrl\n" +
                "FROM Post as p\n" +
                "        join User as u on p.userIdx = u.userIdx\n" +
                "        join PostImgUrl as pi on pi.postIdx = p.postIdx\n" +
                "WHERE pi.status = 'ACTIVE' and p.status = 'ACTIVE' and u.userIdx = ?\n" +
                "group by p.postIdx\n" +
                "order by p.createdAt desc;";

        int se = userIdx;

        return this.jdbcTemplate.query(selectUserPostsQuery,  // List 형태이면 -> query, List가 아니면 queryForObject
                (rs,rowNum) -> new GetUserPostsRes(
                        rs.getInt("postIdx"),
                        rs.getString("postImgUrl")
                ), selectUserPostsQuery);
    }

    public GetUserRes getUsersByEmail(String email){
        String getUsersByEmailQuery = "select userIdx,name,nickName,email from User where email=?";
        String getUsersByEmailParams = email;
        return this.jdbcTemplate.queryForObject(getUsersByEmailQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email"),
                        rs.getString("status")),
                getUsersByEmailParams);
    }


    public GetUserRes getUsersByIdx(int userIdx){
        String getUsersByIdxQuery = "select userIdx,name,nickName,email from User where userIdx=?";
        int getUsersByIdxParams = userIdx;
        return this.jdbcTemplate.queryForObject(getUsersByIdxQuery,
                (rs, rowNum) -> new GetUserRes(
                        rs.getInt("userIdx"),
                        rs.getString("name"),
                        rs.getString("nickName"),
                        rs.getString("email"),
                        rs.getString("status")),
                getUsersByIdxParams);
    }

    public int deleteUsersByIdx(int userIdx){
        String deleteUsersByIdxQuery = "update User set status = 'DELETED' where userIdx = ?";
        int deleteUsersByIdxParams = userIdx;
        return this.jdbcTemplate.update(deleteUsersByIdxQuery,deleteUsersByIdxParams);
    }

    public int createUser(PostUserReq postUserReq){
        String createUserQuery = "insert into User (name, nickName, phone, email, password) VALUES (?,?,?,?,?)";
        Object[] createUserParams = new Object[]{postUserReq.getName(), postUserReq.getNickName(),postUserReq.getPhone(), postUserReq.getEmail(), postUserReq.getPassword()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int checkEmail(String email){
        String checkEmailQuery = "select exists(select email from User where email = ?)";
        String checkEmailParams = email;
        return this.jdbcTemplate.queryForObject(checkEmailQuery,
                int.class,
                checkEmailParams);

    }

    public int checkUserExist(int userIdx){
        String checkUserExistQuery = "select exists(select userIdx from User where userIdx = ?)";
        int checkUserExistParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserExistQuery,
                int.class,
                checkUserExistParams);

    }

    public int modifyUserName(PatchUserReq patchUserReq){
        String modifyUserNameQuery = "update User set nickName = ? where userIdx = ? ";
        Object[] modifyUserNameParams = new Object[]{patchUserReq.getNickName(), patchUserReq.getUserIdx()};

        return this.jdbcTemplate.update(modifyUserNameQuery,modifyUserNameParams);
    }




}
