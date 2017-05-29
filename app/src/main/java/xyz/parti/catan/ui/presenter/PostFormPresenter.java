package xyz.parti.catan.ui.presenter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.model.Parti;
import xyz.parti.catan.data.services.PartiesService;

/**
 * Created by dalikim on 2017. 5. 24..
 */

public class PostFormPresenter extends BasePresenter<PostFormPresenter.View> {
    private final PartiesService partiesService;
    private Disposable loadJoinedParties;
    private List<Parti> joindedParties = new ArrayList<>();
    private ArrayList<SelectedImage> selectedImages = new ArrayList<>();
    private Parti selectedParti;

    public PostFormPresenter(SessionManager session) {
        super();
        partiesService = ServiceBuilder.createService(PartiesService.class, session);
    }

    public void loadJoinedParties() {
        if(!isActive()) return;

        if(joindedParties.size() > 0) {
            getView().resetPartiChoiceList(joindedParties);
            return;
        }

        getView().showPartiChoiceProgressBar();
        loadJoinedParties = getRxGuardian().subscribe(loadJoinedParties,
                partiesService.getMyJoined(),
                response -> {
                    if(!isActive()) return;
                    if(response.isSuccessful()) {
                        joindedParties.clear();
                        joindedParties.addAll(Arrays.asList(response.body()));
                        getView().resetPartiChoiceList(joindedParties);
                    }
                    getView().hidePartiChoiceProgressBar();
                }, error -> {
                    getView().reportError(error);
                    getView().hidePartiChoiceProgressBar();
                });
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
            getView().hidePartiChoice();
            getView().setParti(selectedParti);
        }
    }

    public void showPartiChoice() {
        if(!isActive()) return;

        getView().showPartiChoice();
    }

    public void showPartiChoiceIfNeed() {
        if(!isActive()) return;

        if(selectedParti == null) {
            getView().showPartiChoice();
        } else {
            getView().hidePartiChoice();
        }
    }

    public void savePost() {
        if(!isActive()) return;

        String body = getView().getBody();
        if(TextUtils.isEmpty(body)) return;
        getView().finishAndReturn(selectedParti, body, selectedImages);
    }

    public void setDefaultParti(Parti parti) {
        if(!isActive()) return;

        this.selectedParti = parti;
        getView().setParti(parti);
    }

    public void showImagePicker() {
        if(!isActive()) return;
        
        getView().showImagePicker(selectedImageUris());
    }

    public void removeImage(Uri uri) {
        if(!isActive()) return;

        Iterator<SelectedImage> i = selectedImages.iterator();
        while(i.hasNext()) {
            SelectedImage next = i.next();
            if(next != null && next.uri != null && next.uri.equals(uri)) {
                i.remove();
                break;
            }
        }
        getView().removePreviewImage(uri);
    }

    public void resetImages(List<Uri> uris, List<String> pathes) {
        if(!isActive()) return;

        selectedImages.clear();
        for(int i = 0; i < uris.size(); i++) {
            SelectedImage selected = new SelectedImage();
            selected.path = pathes.get(i);
            selected.uri = uris.get(i);
            selectedImages.add(selected);
        }
        getView().resetPreviewImages(selectedImageUris());
    }

    public interface View {
        void hidePartiChoice();
        void cancelNewPost();
        void setParti(Parti selectedParti);
        void showPartiChoice();
        void hidePartiChoiceProgressBar();
        void showPartiChoiceProgressBar();
        void reportError(Throwable error);
        String getBody();
        Context getContext();
        void finishAndReturn(Parti selectedParti, String body, ArrayList<SelectedImage> fileSourceAttachmentsImages);
        void showImagePicker(ArrayList<Uri> selected);
        void resetPartiChoiceList(List<Parti> joindedParties);
        void resetPreviewImages(List<Uri> imagesUris);
        void removePreviewImage(Uri uri);
    }

    private boolean isActive() {
        return getView() != null;
    }

    private List<String> selectedImagePathes() {
        List<String> result =  new ArrayList<>();
        for(SelectedImage selected : selectedImages) {
            result.add(selected.path);
        }
        return result;
    }

    private ArrayList<Uri> selectedImageUris() {
        ArrayList<Uri> result =  new ArrayList<>();
        for(SelectedImage selected : selectedImages) {
            result.add(selected.uri);
        }
        return result;
    }
}
