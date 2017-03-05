package com.bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{userId}/bookmarks")
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;
    private final AccountRepository accountRepository;

    @Autowired
    BookmarkController(BookmarkRepository bookmarkRepository, AccountRepository accountRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping(value = {"", "/"})
    Resources<BookmarkResource> index(@PathVariable String userId) {
        this.validateUser(userId);
        List<BookmarkResource> bookmarkResourceList = bookmarkRepository
                .findByAccountUsername(userId).stream().map(BookmarkResource::new)
                .collect(Collectors.toList());
        return new Resources<>(bookmarkResourceList);
    }

    @PostMapping(value = {"", "/"})
    ResponseEntity<?> create(@PathVariable String userId, @RequestBody Bookmark input) {
        this.validateUser(userId);

        return this.accountRepository.findByUsername(userId).map(account -> {
            Bookmark bookmark = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));
            Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");

            return ResponseEntity.created(URI.create(forOneBookmark.getHref())).build();
        }).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{bookmarkId}")
    BookmarkResource show(@PathVariable String userId, @PathVariable Long bookmarkId) {
        this.validateUser(userId);

        return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
    }

    private void validateUser(String userId) {
        this.accountRepository.findByUsername(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

}
