package xyz.parti.catan.models;

/**
 * Created by dalikim on 2017. 3. 28..
 */

public class Poll {
    public Long id;
    public String title;
    public Long votings_count;
    public Long agreed_votings_count;
    public Long disagreed_votings_count;
    public User[] latest_agreed_voting_users;
    public User[] latest_disagreed_voting_users;
    public String my_choice;
}
