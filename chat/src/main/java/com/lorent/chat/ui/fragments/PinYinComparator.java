package com.lorent.chat.ui.fragments;

import com.lorent.chat.ui.entity.NearByPeople;
import com.lorent.chat.utils.PinYinUtils;

import java.util.Comparator;

public class PinYinComparator implements Comparator{

	@Override
	public int compare(Object lhs, Object rhs) {
		 String str1 = PinYinUtils.convertChineseToPinYin(((NearByPeople) lhs).getPy());
	     String str2 = PinYinUtils.convertChineseToPinYin(((NearByPeople) rhs).getPy());
	     return str1.compareTo(str2);
	}
	
	
	
}
