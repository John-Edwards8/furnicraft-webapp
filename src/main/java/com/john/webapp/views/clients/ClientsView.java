package com.john.webapp.views.clients;

import com.john.webapp.service.ClientServiceClient;
import com.john.webapp.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Clients")
@Route(value = "clients/:personID?/:action?(edit)", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.ADDRESS_BOOK_SOLID)
//@RolesAllowed("ADMIN")
public class ClientsView extends Div implements BeforeEnterObserver {
	private final String PERSON_EDIT_ROUTE_TEMPLATE = "clients/%s/edit";
    private final ClientServiceClient clientService;
    
    private final ClientGrid grid;
    private final ClientForm form;
    
    public ClientsView(ClientServiceClient clientService) {
        this.clientService = clientService;
        addClassNames("clients-view");

        this.grid = new ClientGrid(clientService);
        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
        	if (event.getValue() != null) {
        		UI.getCurrent().navigate(String.format(PERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
        	} else {
        		UI.getCurrent().navigate(ClientsView.class);
        	}
        });
        this.form = new ClientForm(clientService, grid);

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        grid.createGridLayout(splitLayout);
        form.createEditorLayout(splitLayout);

        add(splitLayout);
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
