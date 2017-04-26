package xyz.parti.catan.models;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dalikim on 2017. 3. 28..
 */

@Parcel
public class Poll {
    public Long id;
    public String title;
    public Long votings_count;
    public Long agreed_votings_count;
    public Long disagreed_votings_count;
    public User[] latest_agreed_voting_users;
    public User[] latest_disagreed_voting_users;
    public String my_choice;

    public boolean isAgreed() {
        return "agree".equals(my_choice);
    }

    public boolean isDisagreed() {
        return "disagree".equals(my_choice);
    }

    public boolean isUnsured() {
        return "unsure".equals(my_choice);
    }

    public boolean isVoted() {
        return my_choice != null;
    }

    public void updateChoice(User someone, String newChoice) {
        String oldChoice = this.my_choice;
        this.my_choice = newChoice;
        switch (newChoice) {
            case "agree":
                latest_agreed_voting_users = addUser(latest_agreed_voting_users, someone);
                latest_disagreed_voting_users = removeUser(latest_disagreed_voting_users, someone);
                agreed_votings_count++;
                if("disagree".equals(oldChoice)) {
                    disagreed_votings_count--;
                }
                break;
            case "disagree":
                latest_agreed_voting_users = removeUser(latest_agreed_voting_users, someone);
                latest_disagreed_voting_users = addUser(latest_disagreed_voting_users, someone);
                disagreed_votings_count++;
                if("agree".equals(oldChoice)) {
                    agreed_votings_count--;
                }
                break;
            case "unsure":
                latest_agreed_voting_users = removeUser(latest_agreed_voting_users, someone);
                latest_disagreed_voting_users = removeUser(latest_disagreed_voting_users, someone);
                if("agree".equals(oldChoice)) {
                    agreed_votings_count--;
                }
                if("disagree".equals(oldChoice)) {
                    disagreed_votings_count--;
                }
                break;
        }
    }

    private User[] removeUser(User[] users, User someone) {
        List<User> userList = new ArrayList<>(Arrays.asList(users));
        Iterator<User> i = userList.iterator();
        while(i.hasNext()) {
            if(i.next().id == someone.id) {
                i.remove();
            }
        }
        return userList.toArray(new User[userList.size()]);
    }

    private User[] addUser(User[] users, User someone) {
        List<User> userList = new ArrayList<>(Arrays.asList(users));
        Iterator<User> i = userList.iterator();
        while(i.hasNext()) {
            if(i.next().id == someone.id) {
                return users;
            }
        }
        userList.add(someone);
        return userList.toArray(new User[userList.size()]);
    }
}
