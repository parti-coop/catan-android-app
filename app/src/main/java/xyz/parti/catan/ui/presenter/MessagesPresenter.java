package xyz.parti.catan.ui.presenter;

import com.google.gson.JsonNull;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.realm.Realm;
import retrofit2.Response;
import xyz.parti.catan.data.ServiceBuilder;
import xyz.parti.catan.data.SessionManager;
import xyz.parti.catan.data.dao.MessagesStatusDAO;
import xyz.parti.catan.data.model.Message;
import xyz.parti.catan.data.model.Page;
import xyz.parti.catan.data.model.User;
import xyz.parti.catan.data.services.MessagesService;
import xyz.parti.catan.helper.CatanLog;
import xyz.parti.catan.helper.RxGuardian;
import xyz.parti.catan.helper.RxHelper;
import xyz.parti.catan.ui.adapter.MessageItem;

/**
 * Created by dalikim on 2017. 6. 25..
 */

public class MessagesPresenter {
    private final Realm realm;
    private final MessagesStatusDAO messagesStatusDAO;
    private final User currentUser;
    private MessagesPresenter.View view;
    private RxGuardian rxGuardian = new RxGuardian();
    private Disposable loadFirstMessages;
    private Disposable loadMoreMessages;
    private Disposable saveReadMessage;
    private MessagesService messagesService;
    private boolean isFirstLoad = true;

    public MessagesPresenter(SessionManager session) {
        messagesService = ServiceBuilder.createService(MessagesService.class, session);
        currentUser = session.getCurrentUser();
        realm = Realm.getDefaultInstance();
        messagesStatusDAO = new MessagesStatusDAO(realm);
    }

    public void attachView(MessagesPresenter.View view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        if(messagesStatusDAO != null) {
            messagesStatusDAO.unwatchAll();
        }
        if(realm != null && !realm.isClosed()) {
            realm.close();
        }
    }

    public MessagesPresenter.View getView() {
        return view;
    }

    private boolean isActive() {
        return view != null;
    }

    public void showMessage(MessageItem item) {
    }

    public void loadFirstMessages() {
        if(!isActive()) return;

        RxHelper.unsubscribe(loadMoreMessages);
        if(isFirstLoad) {
            getView().showDemo();
        }
        loadFirstMessages = rxGuardian.subscribe(loadFirstMessages,
                messagesService.getMessagesLatest(),
                new Consumer<Response<Page<Message>>>() {
                    @Override
                    public void accept(@NonNull Response<Page<Message>> response) throws Exception {
                        if (!isActive()) return;
                        if (response.isSuccessful()) {
                            isFirstLoad = false;
                            Page<Message> messagesPage = response.body();
                            messagesStatusDAO.saveLocalStatus(currentUser, messagesPage.items);
                            saveReadMessage(messagesPage.items);
                            getView().showFirstMessages(messagesPage);
                        } else {
                            getView().reportError("loadFirstMessages in MessagesPresenter");
                            getView().showErrorList();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        getView().reportError(error);
                        getView().showErrorList();
                    }
                });
    }

    private void saveReadMessage(List<Message> messages) {
        long maxId = 0;
        for(Message message : messages) {
            maxId = Math.max(message.id, maxId);
        }

        saveReadMessage = rxGuardian.subscribe(saveReadMessage,
                messagesService.read(maxId),
                new Consumer<Response<JsonNull>>() {
                    @Override
                    public void accept(@NonNull Response<JsonNull> response) throws Exception {
                        /* ignored */
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        CatanLog.e("MessagesPresenter saveReadMessage error", error);
                    }
                });
    }

    public void loadMoreMessages(Message lastMessage) {
        if(!isActive()) return;

        RxHelper.unsubscribe(loadFirstMessages);
        loadMoreMessages = rxGuardian.subscribe(loadMoreMessages,
                messagesService.getMessagesAfter(lastMessage.id),
                new Consumer<Response<Page<Message>>>() {
                    @Override
                    public void accept(@NonNull Response<Page<Message>> response) throws Exception {
                        if (!isActive()) return;
                        if (response.isSuccessful()) {
                            isFirstLoad = false;
                            getView().showMoreMessages(response.body());
                        } else {
                            getView().reportError("loadMoreMessage in MessagesPresenter");
                            getView().showErrorList();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable error) throws Exception {
                        getView().reportError(error);
                        getView().showErrorList();
                    }
                });
    }

    public interface View {
        void showDemo();
        void showErrorList();
        void showFirstMessages(Page<Message> messagesPage);
        void showMoreMessages(Page<Message> body);

        void reportError(Throwable error);
        void reportError(String s);
    }
}
