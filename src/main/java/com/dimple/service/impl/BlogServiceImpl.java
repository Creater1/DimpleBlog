package com.dimple.service.impl;

import com.dimple.bean.Blog;
import com.dimple.bean.BlogExample;
import com.dimple.dao.BlogMapper;
import com.dimple.dao.CategoryMapper;
import com.dimple.dao.CustomMapper;
import com.dimple.framework.enums.BlogStatus;
import com.dimple.service.BlogService;
import com.dimple.utils.FileOperateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BlogServiceImpl
 * @Description:
 * @Auther: Owenb
 * @Date: 12/12/18 14:56
 * @Version: 1.0
 */
@Service
@Slf4j
@Transactional
public class BlogServiceImpl implements BlogService {
    @Autowired
    BlogMapper blogMapper;

    @Autowired
    CustomMapper customMapper;

    @Autowired
    FileOperateUtil fileOperateUtil;

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public List<Blog> selectAllBlog(String title, Date startTime, Date endTime, Integer status) {
        BlogExample blogExample = new BlogExample();
        BlogExample.Criteria criteria = blogExample.createCriteria();
        if (startTime != null && endTime != null) {
            criteria.andCreateTimeBetween(startTime, endTime);
        } else if (startTime != null) {
            criteria.andCreateTimeGreaterThanOrEqualTo(startTime);
        } else if (endTime != null) {
            criteria.andCreateTimeLessThanOrEqualTo(endTime);
        }
        if (StringUtils.isNotBlank(title)) {
            criteria.andTitleLike(title);
        }
        if (status != null) {
            criteria.andStatusEqualTo(status);
        }
        List<Blog> blogs = blogMapper.selectByExample(blogExample);
        return blogs;
    }

    @Override
    public int insertBlog(Blog blog) {
        if (blog == null || StringUtils.isBlank(blog.getTitle()) || StringUtils.isBlank(blog.getContent())) {
            return -1;
        }
        blog.setCreateTime(new Date());
        //设置摘要
        if (StringUtils.isBlank(blog.getSummary())) {
            blog.setSummary(blog.getContent().substring(0, blog.getContent().length() < 150 ? blog.getContent().length() : 150));
        }
        blog.setClick(0);
        blog.setSupport(false);
        blog.setWeight(0);
        blog.setUpdateTime(new Date());
        if (blog.getStatus() == null) {
            //设置为已发表状态
            blog.setStatus(BlogStatus.PUBLISHED.PUBLISHED.getCode());
        }
        //设置博客headerUrl的链接地址（只设置名字）
        blog.setHeaderUrl(fileOperateUtil.getImgName(blog.getHeaderUrl()));
        int i = blogMapper.insert(blog);
        return i;
    }


    @Override
    public int deleteBlog(Integer blogId) {
        if (blogId == null) {
            return -1;
        }
        int i = blogMapper.deleteByPrimaryKey(blogId);
        return i;
    }

    @Override
    public int updateBlog(Blog blog) {
        if (blog.getBlogId() == null || StringUtils.isBlank(blog.getTitle())) {
            return -1;
        }
        int i = blogMapper.updateByPrimaryKeySelective(blog);
        return i;
    }

    @Override
    public int changeBlogStatus(Integer[] ids, Integer status) {
        if (ids == null || ids.length == 0 || status == null) {
            return -1;
        }
        int count = 0;
        for (Integer id : ids) {
            Blog blog = blogMapper.selectByPrimaryKey(id);
            blog.setStatus(status);
            count += blogMapper.updateByPrimaryKeySelective(blog);
        }
        return count;
    }

    @Override
    public Blog selectBlogById(Integer id) {
        if (id == null) {
            return null;
        }
        return blogMapper.selectByPrimaryKey(id);
    }

    @Override
    public Map<String, Integer> selectCountOfBlogStatus() {
        Map<String, Integer> allBolgStatusCount = customMapper.getAllBolgStatusCount();
        return allBolgStatusCount;
    }

    @Override
    public Blog selectBlogByIdBlobs(Integer id) {
        if (id == null) {
            return null;
        }
        Blog blog = blogMapper.selectByPrimaryKey(id);
        return blog;
    }

    @Override
    public int supportBlog(Integer[] ids, Boolean status) {
        if (ids == null || ids.length == 0 || status == null) {
            return -1;
        }
        int count = 0;
        for (Integer id : ids) {
            Blog blog = blogMapper.selectByPrimaryKey(id);
            if (blog != null) {
                blog.setSupport(!status);
                count += blogMapper.updateByPrimaryKey(blog);
            }
        }
        log.info("改变推荐Id为{}的状态从{}到{}", ids, status, !status);
        return count;
    }

    @Override
    public int selectBlogCountByStatus(BlogStatus blogStatus) {
        BlogExample blogExample = new BlogExample();
        if (blogStatus != BlogStatus.ALL) {
            BlogExample.Criteria criteria = blogExample.createCriteria();
            criteria.andStatusEqualTo(blogStatus.getCode());
        }
        int count = blogMapper.countByExample(blogExample);
        return count;
    }
}
