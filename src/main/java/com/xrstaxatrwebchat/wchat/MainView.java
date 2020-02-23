package com.xrstaxatrwebchat.wchat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.xrstaxatrwebchat.wchat.Utils.CorpusProcessor;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.List;

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

//    @Autowired
//    private ComputationGraph net;
//
//    @Autowired
//    private CorpusProcessor corpusProcessor;

    public MainView(UnicastProcessor<ChatMessage> publisher,
                    Flux<ChatMessage> messages) {

        this.publisher = publisher;
        this.messages = messages;
        addClassName("main-view");

        setSizeFull();
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        H1 header = new H1("XrstaXatr chat");
        header.getElement().getThemeList().add("dark");

        add(header);

        askUsername();
    }

    private void askUsername(){
        HorizontalLayout layout = new HorizontalLayout();
        TextField userNameField = new TextField();
        Button startButton = new Button("Enter your name to start chat...");

        layout.add(userNameField, startButton);

        startButton.addClickListener(click -> {
            username = userNameField.getValue();
            remove(layout);
            showChat();
        });

        add(layout);
    }

    private  void showChat(){
        MessageList messageList = new MessageList();

        add(messageList, createInputLayout());
        expand(messageList);



        messages.subscribe(message -> {
            getUI().ifPresent(ui ->
                    ui.access(() ->
                            messageList.add(
                                    new Paragraph(message.getFrom() + ": " +
                                            message.getMessage())
                            )
                    ));

        });

    }

    private Component createInputLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("100%");

        TextField messageField = new TextField();
        TextField botMessageField = new TextField();

        Button sendButton = new Button("Send");
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(messageField, sendButton);
        layout.expand(messageField);

        sendButton.addClickListener(click -> {
            publisher.onNext(new ChatMessage(username, messageField.getValue()));

            messageField.clear();
            messageField.focus();
            publisher.onNext(new ChatMessage("bot", "hi from bot"));
        });



        messageField.focus();

        return layout;
    }


}
