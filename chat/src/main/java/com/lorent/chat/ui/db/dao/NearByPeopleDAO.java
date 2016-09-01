package com.lorent.chat.ui.db.dao;

import com.lorent.chat.ui.entity.NearByPeople;

import java.util.List;

public interface NearByPeopleDAO extends BaseDAO{
	/**
	 * 保存一个用户
	 * @param byPeople
	 */
	public void save(NearByPeople byPeople,String uid);
	
	/**
	 * 根据关系查找
	 * @param type
	 */
	public List<NearByPeople> findByRelationShip(int type,String uid);
	/**
	 * 根据用户号查找
	 * @param uid
	 */
	public NearByPeople findByUid(String uid);
	
	/**
	 * 更新用户信息
	 * @param people
	 */
	public void updatePeople(NearByPeople people,String uid);
}
