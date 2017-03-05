package com.bookmarks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class BookmarkControllerTest {

    private MockMvc mvc;

    private String userName = "nburt";

    private Account account;

    private List<Bookmark> bookmarkList = new ArrayList<>();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void setup() throws Exception {
        this.mvc = webAppContextSetup(webApplicationContext).build();

        this.bookmarkRepository.deleteAllInBatch();
        this.accountRepository.deleteAllInBatch();

        this.account = accountRepository.save(new Account(userName, "password"));
        this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + userName, "A description")));
        this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + userName, "A description")));
    }

    @Test
    public void userNotFound() throws Exception {
        JsonObject bookmark = new JsonObject();

        bookmark.addProperty("uri", "http://bookmark.com/3/foo");
        bookmark.addProperty("description", "a description");

        Gson builder = new GsonBuilder().create();

        String jsonString = builder.toJson(bookmark);

        this.mvc.perform(post("/foo/bookmarks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getBookmark() throws Exception {
        MockHttpServletRequestBuilder request = get("/{userName}/bookmarks/{bookmarkId}", userName, this.bookmarkList.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.id", equalTo(this.bookmarkList.get(0).getId().intValue())))
                .andExpect(jsonPath("$.bookmark.uri", equalTo("http://bookmark.com/1/" + userName)))
                .andExpect(jsonPath("$.bookmark.description", equalTo("A description")));
    }

    @Test
    public void listBookmarks() throws Exception {
        MockHttpServletRequestBuilder request = get("/{userName}/bookmarks", userName)
                .contentType(MediaType.APPLICATION_JSON);

        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bookmarkResourceList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.bookmarkResourceList.[*].bookmark.id", contains(this.bookmarkList.get(0).getId().intValue(), this.bookmarkList.get(1).getId().intValue())))
                .andExpect(jsonPath("$._embedded.bookmarkResourceList.[*].bookmark.uri", contains("http://bookmark.com/1/" + userName, "http://bookmark.com/2/" + userName)))
                .andExpect(jsonPath("$._embedded.bookmarkResourceList.[*].bookmark.description", contains("A description", "A description")));
    }

    @Test
    public void createBookmark() throws Exception {
        JsonObject bookmark = new JsonObject();

        bookmark.addProperty("uri", "http://bookmark.com/3/" + userName);
        bookmark.addProperty("description", "a description");

        Gson builder = new GsonBuilder().create();

        String jsonString = builder.toJson(bookmark);

        MockHttpServletRequestBuilder request = post("/{userName}/bookmarks", userName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString);

        this.mvc.perform(request)
                .andExpect(status().isCreated());
    }

}
