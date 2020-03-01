package com.xrstaxatrwebchat.wchat;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.textfield.TextField;
import com.xrstaxatrwebchat.wchat.Utils.CorpusProcessor;
import com.xrstaxatrwebchat.wchat.Utils.GetInitContext;
import com.xrstaxatrwebchat.wchat.Utils.StringFmtr;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import javax.naming.Context;
import javax.naming.NamingException;
import java.time.LocalDate;
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
@Viewport("width=device-width, height=100vw, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@StyleSheet("frontend://styles/styles.css")
@Route
//@PWA(name = "XrstaXatr Chat", shortName = "xrsta xatr")
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

    @Autowired
    private ComputationGraph net;

    @Autowired
    private CorpusProcessor corpusProcessor;
    private ComponentEventListener componentEventListener;
    private AttachEvent attachEvent;

//    public MainView() {
//        super();
//    }

    @Override
    protected void onAttach(AttachEvent attachEvent){

    }

    public MainView(UnicastProcessor<ChatMessage> publisher,
                    Flux<ChatMessage> messages) {

        this.publisher = publisher;
        this.messages = messages;
        addClassName("main-view");

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H1 header = new H1("XrstaXatr \\{o_o}/ ... a chatbot with christian sensibilities");
        header.getElement().getThemeList().add("dark");

        add(header);

        askUsername();

    }

    private void askUsername(){
        HorizontalLayout layout = new HorizontalLayout();
        TextField userNameField = new TextField();
        Button startButton = new Button("Enter your name to start chat...");
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

    private  void showChat(){

        UI currUi = UI.getCurrent();






        MessageList messageList = new MessageList();

        add(messageList, createInputLayout());
        expand(messageList);



        String mostPreviousMessage = "";

//        messages.subscribe(message -> {
//            getUI().ifPresent(ui ->
//                    ui.access(() ->
//                            messageList.add(
//                                    new Paragraph(message.getFrom() + ": " +
//                                            message.getMessage())
//                            )
//                    ));
//
//        });


        super.onAttach();

        messages.subscribe(message -> {
            try{

                currUi.getSession();
                getUI().ifPresent(ui ->
                        ui.access(() ->
                                messageList.add(
                                        new Paragraph(message.getFrom() + ": " +
                                                message.getMessage())
                                )
                        ));

            }catch(UIDetachedException uide){
                System.out.println("Error ui detached: "  );
//                uide.printStackTrace();
//                uide.getLocalizedMessage();
                HorizontalLayout layout = new HorizontalLayout();
                TextField userNameField = new TextField();
                add(layout);
                showChat();
                messages.subscribe(msg -> {
                    getUI().ifPresent(ui ->
                        ui.access(() ->
                            messageList.add(
                                    new Paragraph(msg.getFrom() + ": " +
                                            msg.getMessage())
                            )
                        ));

                    });
            }
        });
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

            System.out.println("provided userid: "+username);
//            System.out.println("ack: "+ackId);
            System.out.println("provided userResponse: "+userMessage+"\n");
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
