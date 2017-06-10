package xyz.parti.catan.data.model;

import java.util.Date;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class Page<T> {
    public boolean has_more_item;
    public Date last_stroked_at;
    public List<T> items;
 }
