package com.example.demo.src.post;

import com.example.demo.config.BaseException;
import com.example.demo.src.post.model.GetPostsRes;
import com.example.demo.src.post.model.PatchPostsReq;
import com.example.demo.src.post.model.PostPostsReq;
import com.example.demo.src.post.model.PostPostsRes;
import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.demo.config.BaseResponseStatus.*;

@Service
public class PostService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PostDao postDao;
    private final PostProvider postProvider;
    private final JwtService jwtService;

    @Autowired
    public PostService(PostDao postDao, PostProvider postProvider, JwtService jwtService) {
        this.postDao = postDao;
        this.postProvider = postProvider;
        this.jwtService = jwtService;
    }

    public PostPostsRes createPosts(int userIdx, PostPostsReq postPostsReq) throws BaseException {

        try{

            int postIdx = postDao.insertPosts(userIdx, postPostsReq.getContent());

            for(int i = 0; i < postPostsReq.getPostImgUrls().size(); i++) {
                postDao.insertPostImgs(postIdx, postPostsReq.getPostImgUrls().get(i));
            }

            return new PostPostsRes(postIdx);
        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void modifyPost(int userIdx, int postIdx, PatchPostsReq patchPostsReq) throws BaseException {
        if(postProvider.checkUserExist(userIdx)==0) {
            throw new BaseException(USERS_EMPTY_USER_ID);
        }

        if(postProvider.checkPostExist(postIdx)==0) {
            throw new BaseException(POSTS_EMPTY_POST_ID);
        }

        try{

            int result = postDao.updatePost(postIdx, patchPostsReq.getContent());
            if(result==0) {
                throw new BaseException(MODIFY_FAIL_POST);
            }

        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void deletePost(int postIdx) throws BaseException {

        try{

            int result = postDao.deletePost(postIdx);
            if(result==0) {
                throw new BaseException(DELETE_FAIL_POST);
            }

        }
        catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
