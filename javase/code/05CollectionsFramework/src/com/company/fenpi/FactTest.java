package com.company.fenpi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FactTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		List<Map<String,Object>> list=new ArrayList<>();
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("id", 1);
		map.put("name", "南天");
		list.add(map);
		Map<String,Object> map1=new HashMap<String, Object>();
		map1.put("id", 2);
		map1.put("name","2");
		map1.put("parentId", 1);
		list.add(map1);
		Map<String,Object> map2=new HashMap<String, Object>();
		map2.put("id", 3);
		map2.put("name","3");
		map2.put("parentId", 2);
		list.add(map2);
		Map<String,Object> map3=new HashMap<String, Object>();
		map3.put("id", 4);
		map3.put("name","4");
		map3.put("parentId", 3);
		list.add(map3);
		Map<String,Object> map4=new HashMap<String, Object>();
		map4.put("id", 5);
		map4.put("name","5");
		map4.put("parentId", 4);
		list.add(map4);
		List<Integer> dep=new ArrayList<>();
		dep.add(4);
		dep.add(3);
		List<Integer> dep1=new ArrayList<>();
		dep1.add(5);
		List<Map<String,Object>> userList=new ArrayList<>();
		Map<String,Object> userMap=new HashMap<String, Object>();
		userMap.put("id", 1);
		userMap.put("name", "南天");
		userMap.put("department", dep);
		userList.add(userMap);
		Map<String,Object> userMap2=new HashMap<String, Object>();
		userMap2.put("id", 1);
		userMap2.put("name", "南天");
		userMap2.put("department", dep);
		userList.add(userMap2);
		Map<String,Object> userMap1=new HashMap<String, Object>();
		userMap1.put("id", 2);
		userMap1.put("name", "南天2");
		userMap1.put("department", dep1);
		userList.add(userMap1);
		Map<String,Object> userMap3=new HashMap<String, Object>();
		userMap3.put("id", 2);
		userMap3.put("name", "南天2");
		userMap3.put("department", dep1);
		userList.add(userMap3);
		System.out.println("递归之前:"+list);
		for(Map<String,Object> dMap:list){
			Integer depId=Integer.valueOf(String.valueOf(dMap.get("id")));
			String depName=departmentName(list,depId);
			boolean flag=true;
			if(depId!=1){
				while(flag){
					Integer parentId=factParentId(list,depId);
					if(parentId==1){
						flag=false;
					}else{
						depName=departmentName(list,parentId)+"-"+depName;
					}
					depId=parentId;
				}
			}
			dMap.put("departmentName", depName);
		}
		System.out.println("递归之后:"+list);
System.out.println("=========");
	Integer departmentId=null;

	for(Map<String,Object> mapp:userList){
		String departmentName="";
		List<Integer> depIdList=(List<Integer>) mapp.get("department");
		List<Integer> sortList=factbabyList(list,depIdList);
		departmentId=sortList.get(sortList.size()-1);
		for(Integer id:sortList){
			for(Map<String,Object> depMap:list){
				Integer depId=Integer.valueOf(String.valueOf(depMap.get("id")));
				if(id.equals(depId)){
					departmentName=String.valueOf(depMap.get("departmentName"))+departmentName;
				}
			}
		}
		System.out.println("pp"+departmentName);
	}


		/*List<Integer> depIdList=new ArrayList<>();
		depIdList.add(3);
		depIdList.add(4);
		depIdList.add(5);
		depIdList.add(2);
		List<Integer> resultList=factbabyList(list,depIdList);
		System.out.println("---"+resultList);
		Integer depIpd=0;
		String depName="";
		for(Integer id:depIdList){
			boolean flag=factSubDepList(list,id);
			if(flag){
				depIpd=id;
			}
		}
		System.out.println(depIpd);*/
	}
	public static Integer factParentId(List<Map<String,Object>> list,Integer depId){
		Integer parentId=0;
		for(Map<String,Object> map:list){
			Integer id=Integer.valueOf(String.valueOf(map.get("id")));
             	if(id!=1&&depId.equals(id)){
				parentId=Integer.valueOf(String.valueOf(map.get("parentId")));
			}
		}
		return parentId;
	}
	
	public static int factList(List<Map<String,Object>> list,Integer parentId){
		int depparentId=0;
		for(Map<String,Object> dMap:list){
			Integer id=Integer.valueOf(String.valueOf(dMap.get("id")));
			if(parentId.equals(id)){
				Integer depId=Integer.valueOf(String.valueOf(dMap.get("parentId")));
				if(depId!=1){
					depparentId=factList(list,depId);
					if(depparentId==1){
						depparentId=depId;
					}
				}else{
					return parentId;
				}
			}
		}
		return depparentId;
	}
	public static String departmentName(List<Map<String,Object>> list,Integer depId){
		String depName="";
		for(Map<String,Object> dMap:list){
			Integer id=Integer.valueOf(String.valueOf(dMap.get("id")));
			if(depId.equals(id)){
				depName=String.valueOf(dMap.get("name"));
				}
			}
		return depName;
	}
	public static List<Integer> factbabyList(List<Map<String,Object>> list,List<Integer> depIdList){
		 Integer parentId=0;
		 List<Integer> sortDepId=new ArrayList<>();
	        for(Integer id:depIdList){
	            if (!depIdList.contains(factParentId(list,id))) {
	            	parentId=id;
	            	sortDepId.add(parentId);
	            }
	        }
	        for(Integer id:depIdList){
	        	if(!id.equals(parentId)){
	        		sortDepId.add(id);
	        	}
	        }
		return sortDepId;
	}
	public static boolean factSubDepList(List<Map<String,Object>> list,Integer parentId){
		boolean flag=true;
		for(Map<String,Object> dMap:list){
			Integer id=Integer.valueOf(String.valueOf(dMap.get("id")));
			if(parentId.equals(factParentId(list,id))){
				flag=false;
			}
		}
		return flag;
	}
}
