package cmp;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ListToString{
	public static String listToString(List list, char separator) {
		return StringUtils.join(list.toArray(), separator);
 }
}