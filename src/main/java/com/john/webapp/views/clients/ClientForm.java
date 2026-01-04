package com.john.webapp.views.clients;

import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.service.ClientServiceClient;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

public class ClientForm extends FormLayout {
	private final BeanValidationBinder<ClientResponseDto> binder =
            new BeanValidationBinder<>(ClientResponseDto.class);
	private ClientResponseDto person;

    private TextField name;
    private TextField surname;
    private TextField patronymic;
    private TextField phoneNumber;
    private TextField email;
    //private DatePicker dateOfBirth;
    
    private Runnable onClose;
    private final Button save = new Button("Save");

	public ClientForm(ClientServiceClient clientService, ClientGrid grid) {
		super();
		
        save.addClickListener(e -> {
            try {
                if (this.person == null) {
                    this.person = new ClientResponseDto();
                }
                
                binder.writeBean(this.person);
                
                if (this.person.getId() == null) {
                    clientService.createClient(this.person);
                } else {
                    clientService.updateClient(this.person);
                }
                
                binder.readBean(null);
                grid.refreshGrid();
                Notification.show("Data updated");
                
                if (onClose != null) {
                    onClose.run();
                }
                
                UI.getCurrent().navigate(ClientsView.class);
            } catch (Exception exception) {
                Notification n = Notification.show(exception.getMessage());
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
	}
	
	public void setOnClose(Runnable callback) {
        this.onClose = callback;
    }
	
	public void clearForm() {
		this.person = null;
	    binder.setBean(null);
	}
	public void editClient(ClientResponseDto client) {
	    this.person = client;
	    binder.setBean(client);
	}
	
	public void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("First Name");
        surname = new TextField("Last Name");
        patronymic = new TextField("Patronymic");
        phoneNumber = new TextField("Phone");
        email = new TextField("Email");
        //dateOfBirth = new DatePicker("Date Of Birth");
        formLayout.add(name, surname, patronymic, phoneNumber, email/*, dateOfBirth*/);
        
        binder.bindInstanceFields(this);
        
        editorDiv.add(formLayout);
        
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save);
        editorLayoutDiv.add(buttonLayout);

        splitLayout.addToSecondary(editorLayoutDiv);
    }
}
