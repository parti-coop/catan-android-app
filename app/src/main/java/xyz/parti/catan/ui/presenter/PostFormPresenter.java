package xyz.parti.catan.ui.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import io.reactivex.disposables.Disposable;
import retrofit2.Response;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Group;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.services.PartiesService;
import xyz.parti.catan.ui.adapter.PostFormGroupItem;
import xyz.parti.catan.ui.adapter.PostFormPartiItem;

/**
 * Created by dalikim on 2017. 5. 24..
 */

public class PostFormPresenter extends BasePresenter<PostFormPresenter.View> {
    private final PartiesService partiesService;
    private Disposable loadJoinedParties;
    private List<AbstractItem> items = new ArrayList<>();
    private Parti selectedParti;

    public PostFormPresenter(SessionManager session) {
        super();
        partiesService = ServiceBuilder.createService(PartiesService.class, session);
    }

    public void loadJoinedParties(FastItemAdapter<AbstractItem> fastAdapter) {
        if(!isActive()) return;

        fastAdapter.clear();
        if(items.size() > 0) {
            fastAdapter.add(items);
            return;
        }

        getView().showPartiChoiceProgressBar();
        loadJoinedParties = getRxGuardian().subscribe(loadJoinedParties,
                partiesService.getMyJoined(),
                response -> {
                    if(!isActive()) return;
                    if(response.isSuccessful()) {
                        TreeMap<Group, List<PostFormPartiItem>> result = getGroupList(response);

                        items.clear();
                        for(Group group: result.keySet()) {
                            if(group.isIndie()) {
                                items.add(0, new PostFormGroupItem(group));
                                items.addAll(1, result.get(group));
                            } else {
                                items.add(new PostFormGroupItem(group));
                                items.addAll(result.get(group));
                            }
                        }

                        fastAdapter.clear();
                        fastAdapter.add(items);
                    }
                    getView().hidePartiChoiceProgressBar();
                }, error -> {
                    getView().reportError(error);
                    getView().hidePartiChoiceProgressBar();
                });
    }

    @NonNull
    private TreeMap<Group, List<PostFormPartiItem>> getGroupList(Response<Parti[]> response) {
        TreeMap<Group, List<PostFormPartiItem>> result = new TreeMap<>();
        for(Parti parti : response.body()) {
            List<PostFormPartiItem> parties = result.get(parti.group);
            if(parties == null) {
                parties = new ArrayList<>();
            }
            parties.add(new PostFormPartiItem(parti));
            result.put(parti.group, parties);
        }
        return result;
    }

    public void selectParti(Parti parti) {
        if(!isActive()) return;
        if(parti != null) {
            selectedParti = parti;
        }

        updateParti();
    }

    public void cancelPartiChoice() {
        updateParti();
    }

    private void updateParti() {
        if(!isActive()) return;

        if(selectedParti == null) {
            getView().cancelNewPost();
        } else {
            getView().closePartiChoiceDialog();
            getView().setParti(selectedParti);
        }
    }

    public void showPartiChoice() {
        if(!isActive()) return;

        getView().showPartiChoice();
    }

    public void savePost() {
        if(!isActive()) return;

        String body = getView().getBody();
        getView().finishAndReturn(selectedParti, body);
    }

    public void setDefaultParti(Parti parti) {
        if(!isActive()) return;

        this.selectedParti = parti;
        getView().setParti(parti);
    }

    public interface View {
        void closePartiChoiceDialog();
        void cancelNewPost();
        void setParti(Parti selectedParti);
        void showPartiChoice();
        void hidePartiChoiceProgressBar();
        void showPartiChoiceProgressBar();
        void reportError(Throwable error);
        String getBody();
        Context getContext();
        void finishAndReturn(Parti selectedParti, String body);
    }

    private boolean isActive() {
        return getView() != null;
    }
}
