package xyz.parti.catan.data.model;

import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class Page<T> {
    public boolean has_more_item;
    public List<T> items;
 }
