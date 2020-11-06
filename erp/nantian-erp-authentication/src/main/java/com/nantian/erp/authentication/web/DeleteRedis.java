package  com.nantian.erp.authentication.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nantian.erp.common.base.util.RestUtils;
import com.nantian.erp.common.rest.RestResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/** 
 * Description: 删除Redis的值a
 */
@RestController
@Api(value = "删除Redis")
@RequestMapping(value = "/authentication/redis")
public class DeleteRedis {
	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisTemplate redisTemplate;
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/deleteToken")
	@ApiOperation(value = "删除token", notes = "删除token")
	public RestResponse deleteToken(@RequestParam String token) {
		boolean result = redisTemplate.delete(token);
		return RestUtils.returnSuccess(result);
	}
	
}
