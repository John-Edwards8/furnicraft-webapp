package com.john.webapp.views.clients;

import com.john.webapp.dto.ClientResponseDto;
import com.john.webapp.service.ClientServiceClient;
import com.john.webapp.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;

import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Clients")
@Route(value = "clients/:personID?/:action?(edit)", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.ADDRESS_BOOK_SOLID)
//@RolesAllowed("ADMIN")
public class ClientsView extends Div implements BeforeEnterObserver {
	enum EditorState {
	    CLOSED,
	    CREATE,
	    EDIT
	}
	
	private EditorState editorState = EditorState.CLOSED;

	private final String PERSON_EDIT_ROUTE_TEMPLATE = "clients/%s/edit";
    private final ClientServiceClient clientService;
    
    private final ClientGrid grid;
    private final ClientForm form;
    private final SplitLayout splitLayout;
    private final Button floatingButton;
    private final Div floatingActions;
    
    public ClientsView(ClientServiceClient clientService) {
        this.clientService = clientService;
        addClassNames("clients-view");
        setSizeFull();
        getStyle().set("position", "relative");
        
        this.grid = new ClientGrid(clientService);
        this.form = new ClientForm(clientService, grid);
        
        // Create UI
        splitLayout = new SplitLayout();
        splitLayout.setSplitterPosition(100);
        grid.createGridLayout(splitLayout);
        form.createEditorLayout(splitLayout);
        
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
            	UI.getCurrent().navigate(String.format(PERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
                openEdit(event.getValue());
            } else {
            	UI.getCurrent().navigate(ClientsView.class);
                closeEditor();
            }
        });
        
        floatingButton = createFloatingButton();

        floatingActions = new Div(floatingButton);
        floatingActions.addClassName("clients-floating-actions");
        form.setOnClose(() -> closeEditor());

        add(splitLayout, floatingActions);
    }
    
    private Button createFloatingButton() {
        Button button = new Button(new Icon(VaadinIcon.PLUS));
        button.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_LARGE
        );

        button.setTooltipText("Create client");
        button.getStyle()
                .set("border-radius", "50%")
                .set("width", "48px")
                .set("height", "48px");

        button.addClickListener(e -> {
            if (editorState == EditorState.CLOSED) {
                openCreate();
            } else {
                closeEditor();
            }
        });

        return button;
    }
    
    private void openCreate() {
        editorState = EditorState.CREATE;

        form.clearForm();
        splitLayout.setSplitterPosition(70);

        floatingButton.setIcon(new Icon(VaadinIcon.CLOSE));
        floatingButton.setTooltipText("Close editor");

        floatingActions.getStyle()
                .set("right", "calc(30% + var(--lumo-space-m))")
                .set("bottom", "var(--lumo-space-l)");
    }
    
    private void openEdit(ClientResponseDto client) {
        editorState = EditorState.EDIT;

        form.editClient(client);
        splitLayout.setSplitterPosition(70);

        floatingButton.setIcon(new Icon(VaadinIcon.CLOSE));
        floatingButton.setTooltipText("Close editor");

        floatingActions.getStyle()
                .set("right", "calc(30% + var(--lumo-space-m))")
                .set("bottom", "var(--lumo-space-l)");
    }

    
    private void closeEditor() {
        editorState = EditorState.CLOSED;

        form.clearForm();
        grid.asSingleSelect().clear();
        splitLayout.setSplitterPosition(100);

        floatingButton.setIcon(new Icon(VaadinIcon.PLUS));
        floatingButton.setTooltipText("Create client");

        floatingActions.getStyle()
                .set("right", "var(--lumo-space-m)")
                .set("bottom", "var(--lumo-space-xl)");
    }
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> personId = event.getRouteParameters().get("personID").map(Long::parseLong);
        if (personId.isPresent()) {
            clientService.getClientById(personId.get())
                    .ifPresentOrElse(
                            form::editClient,
                            () -> {
                                Notification.show("Client not found");
                                event.forwardTo(ClientsView.class);
                            }
                    );
        } else {
            form.clearForm();
        }
    }
}
