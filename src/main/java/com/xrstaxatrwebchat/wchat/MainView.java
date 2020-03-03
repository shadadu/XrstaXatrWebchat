package com.xrstaxatrwebchat.wchat;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.internal.CurrentInstance;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.xrstaxatrwebchat.wchat.Utils.CorpusProcessor;
import com.xrstaxatrwebchat.wchat.Utils.GetInitContext;
import com.xrstaxatrwebchat.wchat.Utils.StringFmtr;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import javax.naming.Context;
import javax.naming.NamingException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@PageTitle("xrstaxatr")
@Viewport("width=device-width, height=100vw, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@StyleSheet("frontend://styles/styles.css")
@Route
@Push
public class MainView extends VerticalLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     */

    private UnicastProcessor<ChatMessage> publisher;
    private Flux<ChatMessage> messages;
    private String username;
    private List<String> currentUsers;
    private UI savedUI;
    private Logger logger = Logger.getLogger("MainView-logger");
    private Thread holdThread;

    @Autowired
    private ComputationGraph net;

    @Autowired
    private CorpusProcessor corpusProcessor;

    public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {

        this.publisher = publisher;
        this.messages = messages;
        addClassName("main-view");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        setPadding(true);
        setMargin(true);

        H1 header = new H1("XrstaXatr \\{o_o}/ ... a chatbot with christian sensibilities");
        header.getElement().getStyle().set("text-align", "center");
//        header.getElement().getStyle().set("font-size", "5");

        header.getElement().getThemeList().add("dark");

        add(header);

        askUsername();

    }

    private void askUsername(){

//        HorizontalLayout layout = new HorizontalLayout();
        VerticalLayout layout = new VerticalLayout();
        TextField userNameField = new TextField();
        Button startButton = new Button("Enter your name to start chat...");

        layout.setHorizontalComponentAlignment(Alignment.CENTER, userNameField);
        layout.setHorizontalComponentAlignment(Alignment.CENTER, startButton);


        List<String> RESERVED_NAMES = Arrays.asList("bot", "chatbot", "moderator", "imod");

        layout.add(userNameField, startButton);

        startButton.addClickListener(click -> {

            if(userNameField.getValue().isEmpty()){
                username = "anon" + new Random( new Date().getTime()).nextInt();
            }else{
                username = userNameField.getValue();
            }
            remove(layout);
            showChat();
        });

        getUI().ifPresent(ui -> ui.setPollInterval((int) TimeUnit.MINUTES.toMillis(1)));
        add(layout);
    }

    private  void showChat() {

        UI currUi = UI.getCurrent();

        VaadinSession session  = currUi.getSession();

        logger.info("currUi id: "+currUi.getUIId());
        MessageList messageList = new MessageList();

        add(messageList, createInputLayout());
        expand(messageList);

        messages.subscribe( msg -> {

            try{
                logger.info("message w/o new thread");
                getUI().ifPresent(ui ->
                        ui.access(() -> messageList.add(
                                new Paragraph(msg.getFrom() + ": " +
                                        msg.getMessage())
                                )
                        ));
                savedUI = getUI().get();
                holdThread = Thread.currentThread();
            }catch (  Throwable  throwable ){
//                Exceptions.isErrorCallbackNotImplemented(throwable);
                logger.info("Got UIDetachedException");
                logger.info("session is NULL");


                new Thread(()->{

                    logger.info("new thread spawned");
                    UI.setCurrent(savedUI);
                    VaadinSession.setCurrent(savedUI.getSession());
                    messages.subscribe( message -> {
                                logger.info("message w/ new thread created");
                                savedUI.access( () ->messageList.add(
                                        new Paragraph(message.getFrom() + ": " +
                                                message.getMessage()) ));
                            }

                    );
                }).start();
//                throw new UIDetachedException();
            }
            }

        );




//        if( session == null ){
//            logger.info("session is NULL");
//            new Thread(()->{
//
//                logger.info("new thread spawned");
//                UI.setCurrent(savedUI);
//                VaadinSession.setCurrent(savedUI.getSession());
//                messages.subscribe( message -> {
//                    logger.info("message w/ new thread created");
//                    savedUI.access( () ->messageList.add(
//                                    new Paragraph(message.getFrom() + ": " +
//                                            message.getMessage()) ));
//                        }
//
//                );
//            }
//
//            ).start();
//
//        }else{
//            logger.info("session is NOT NULL");
//            messages.subscribe( msg -> {
//                logger.info("message w/o new thread");
//                getUI().ifPresent(ui ->
//                        ui.access(() -> messageList.add(
//                                new Paragraph(msg.getFrom() + ": " +
//                                        msg.getMessage())
//                                )
//                        ));
//                        savedUI = getUI().get();
//                }
//            );
//        }

//        messages.subscribe( msg -> {
//            getUI().ifPresent(ui ->
//
//                            ui.access(() ->
//                                    messageList.add(
//                                            new Paragraph(msg.getFrom() + ": " +
//                                                    msg.getMessage())
//                                    )
//
//                            ));
//                    savedUI = getUI().get();
//                }
//
//        );

    }

    private Component createInputLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("80%");

        TextField messageField = new TextField();
        TextField botMessageField = new TextField();

        Button sendButton = new Button("Send");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(messageField, sendButton);
        layout.expand(messageField);

        sendButton.addClickListener(click -> {
            String userMessage = messageField.getValue();
            String ackId = String.valueOf(Math.abs(new Random(1234).nextLong()));

            publisher.onNext(new ChatMessage(username, messageField.getValue()));

            String botReply = "";
            String userMsgTimeRcvd = LocalDateTime.now().toString();

            logger.info("provided userid: "+username);
//            System.out.println("ack: "+ackId);
            logger.info("provided userResponse: "+userMessage+"\n");
            String userResponseFmtd = StringFmtr.inputSanitizer(userMessage.toLowerCase());
            String userIdFmtd = StringFmtr.inputSanitizer(username);
            String ackIdFmtd = StringFmtr.inputSanitizer(ackId);

            try{

                Context confContext = new GetInitContext().fun();
                botReply = new BotResponse(confContext, 0.0).getBotResponse(userResponseFmtd, net, corpusProcessor);

                System.out.println("botReply: "+botReply);

                MessagePojo userMsgPojo = new MessagePojo(userIdFmtd, "bot", userResponseFmtd, userMsgTimeRcvd, ackIdFmtd);
                MessagePojo botMsgPojo = new MessagePojo("bot", userIdFmtd, botReply, LocalDateTime.now().toString(), ackIdFmtd);

                JsonTasks.jsonArrayAppend(userMsgPojo, botMsgPojo, confContext);

                ClassLoader contxClassLoader = Thread.currentThread().getContextClassLoader();

                try{
                    if(confContext.getClass().getClassLoader() == contxClassLoader){
                        confContext.close();
                    }
                }catch(NamingException ne){
                    System.out.println("Error removing contexloader "+ne.getExplanation());
                    ne.printStackTrace();
                }

                confContext.close();

            }catch (OutOfMemoryError | Exception e){
                Logger.getLogger("Encountered an error during response generation");
                e.printStackTrace();
            }

            publisher.onNext(new ChatMessage("\\{o_o}/ ", botReply));
            messageField.clear();
            messageField.focus();

        });


        messageField.focus();

        return layout;
    }


}
