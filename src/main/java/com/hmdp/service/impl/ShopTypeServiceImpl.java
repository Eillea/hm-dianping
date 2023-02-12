package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        //使用list取 【0 -1】 代表全部
        List<String> shopTypeList = stringRedisTemplate.opsForList().range("shop:list", 0, -1);
        if (CollectionUtil.isNotEmpty(shopTypeList)) {
            //shopTypeList.get(0) 其实是获取了整个List集合里的元素
            List<ShopType> types = JSONUtil.toList(shopTypeList.get(0), ShopType.class);
            return Result.ok(types);
        }


//        //使用string 取
//        String shoptype = redisTemplate.opsForValue().get("shop:type");
//        if (!StrUtil.isEmpty(shoptype)){
//            List<ShopType> typeList = JSONUtil.toList(shoptype, ShopType.class);
//            return Result.ok(typeList);
//        }


        List<ShopType> typeList = query().orderByAsc("sort").list();

        if (CollectionUtil.isEmpty(typeList)) {
            return Result.fail("列表信息不存在");
        }

        //string 存
//        redisTemplate.opsForValue().set("shop:type,JSONUtil.toJsonStr(typeList));

        //list 存
        String jsonStr = JSONUtil.toJsonStr(typeList);
        stringRedisTemplate.opsForList().leftPushAll("shop:list", jsonStr);

        return Result.ok(typeList);
    }

}
