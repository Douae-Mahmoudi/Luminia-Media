package com.collection.controller;

import com.collection.domain.Collection;
import com.collection.usecase.collection.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    private MockMvc mockMvc;

    @Mock private CreateCollectionUseCase          createCollectionUseCase;
    @Mock private GetCollectionUseCase             getCollectionUseCase;
    @Mock private AddMediaToCollectionUseCase      addMediaToCollectionUseCase;
    @Mock private RemoveMediaFromCollectionUseCase removeMediaFromCollectionUseCase;
    @Mock private DeleteCollectionUseCase          deleteCollectionUseCase;

    @InjectMocks
    private CollectionController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String USER_ID  = "user-123";
    private final String COLL_ID  = "coll-456";
    private final String MEDIA_ID = "media-789";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(USER_ID, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Test
    void create_ShouldReturnOk() throws Exception {
        CreateCollectionUseCase.Input input =
                new CreateCollectionUseCase.Input(null, "Ma Liste", "Desc", true);
        Collection created = new Collection(COLL_ID, USER_ID, "Ma Liste", "Desc", true);

        when(createCollectionUseCase.execute(any())).thenReturn(created);

        mockMvc.perform(post("/api/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COLL_ID))
                .andExpect(jsonPath("$.name").value("Ma Liste"));
    }


    @Test
    void getById_ShouldReturnCollection() throws Exception {
        Collection collection = new Collection(COLL_ID, USER_ID, "Favs", "Desc", true);
        when(getCollectionUseCase.getById(COLL_ID)).thenReturn(collection);

        mockMvc.perform(get("/api/collections/{id}", COLL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COLL_ID));
    }


    @Test
    void getByUserId_ShouldReturnList() throws Exception {
        List<Collection> collections = List.of(
                new Collection(COLL_ID, USER_ID, "Favs", "Desc", true)
        );
        when(getCollectionUseCase.getByUserId(USER_ID)).thenReturn(collections);

        mockMvc.perform(get("/api/collections/user/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(COLL_ID));
    }


    @Test
    void getAllPublic_ShouldReturnList() throws Exception {
        List<Collection> collections = List.of(
                new Collection(COLL_ID, USER_ID, "Favs", "Desc", true)
        );
        when(getCollectionUseCase.getAllPublic()).thenReturn(collections);

        mockMvc.perform(get("/api/collections/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(COLL_ID));
    }


    @Test
    void getPublicByUserId_ShouldReturnList() throws Exception {
        List<Collection> collections = List.of(
                new Collection(COLL_ID, USER_ID, "Favs", "Desc", true)
        );
        when(getCollectionUseCase.getPublicByUserId(USER_ID)).thenReturn(collections);

        mockMvc.perform(get("/api/collections/user/{userId}/public", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(COLL_ID));
    }


    @Test
    void addMedia_ShouldReturnUpdatedCollection() throws Exception {
        Collection updated = new Collection(COLL_ID, USER_ID, "Favs", "Desc", true);
        when(addMediaToCollectionUseCase.execute(any())).thenReturn(updated);

        mockMvc.perform(post("/api/collections/{collectionId}/media/{mediaId}", COLL_ID, MEDIA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COLL_ID));
    }


    @Test
    void deleteCollection_ShouldReturnNoContent() throws Exception {
        doNothing().when(deleteCollectionUseCase).execute(COLL_ID);

        mockMvc.perform(delete("/api/collections/{id}", COLL_ID))
                .andExpect(status().isNoContent());

        verify(deleteCollectionUseCase, times(1)).execute(COLL_ID);
    }


    @Test
    void removeMedia_ShouldReturnUpdatedCollection() throws Exception {
        Collection updated = new Collection(COLL_ID, USER_ID, "Favs", "Desc", true);
        when(removeMediaFromCollectionUseCase.execute(any())).thenReturn(updated);

        mockMvc.perform(delete("/api/collections/{collectionId}/media/{mediaId}", COLL_ID, MEDIA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COLL_ID));
    }
}
