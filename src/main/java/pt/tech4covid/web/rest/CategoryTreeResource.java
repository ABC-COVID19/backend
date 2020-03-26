package pt.tech4covid.web.rest;

import pt.tech4covid.domain.CategoryTree;
import pt.tech4covid.service.CategoryTreeService;
import pt.tech4covid.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link pt.tech4covid.domain.CategoryTree}.
 */
@RestController
@RequestMapping("/api")
public class CategoryTreeResource {

    private final Logger log = LoggerFactory.getLogger(CategoryTreeResource.class);

    private static final String ENTITY_NAME = "icamApiCategoryTree";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CategoryTreeService categoryTreeService;

    public CategoryTreeResource(CategoryTreeService categoryTreeService) {
        this.categoryTreeService = categoryTreeService;
    }

    /**
     * {@code POST  /category-trees} : Create a new categoryTree.
     *
     * @param categoryTree the categoryTree to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new categoryTree, or with status {@code 400 (Bad Request)} if the categoryTree has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/category-trees")
    public ResponseEntity<CategoryTree> createCategoryTree(@RequestBody CategoryTree categoryTree) throws URISyntaxException {
        log.debug("REST request to save CategoryTree : {}", categoryTree);
        if (categoryTree.getId() != null) {
            throw new BadRequestAlertException("A new categoryTree cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CategoryTree result = categoryTreeService.save(categoryTree);
        return ResponseEntity.created(new URI("/api/category-trees/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /category-trees} : Updates an existing categoryTree.
     *
     * @param categoryTree the categoryTree to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated categoryTree,
     * or with status {@code 400 (Bad Request)} if the categoryTree is not valid,
     * or with status {@code 500 (Internal Server Error)} if the categoryTree couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/category-trees")
    public ResponseEntity<CategoryTree> updateCategoryTree(@RequestBody CategoryTree categoryTree) throws URISyntaxException {
        log.debug("REST request to update CategoryTree : {}", categoryTree);
        if (categoryTree.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CategoryTree result = categoryTreeService.save(categoryTree);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, categoryTree.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /category-trees} : get all the categoryTrees.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of categoryTrees in body.
     */
    @GetMapping("/category-trees")
    public ResponseEntity<List<CategoryTree>> getAllCategoryTrees(Pageable pageable) {
        log.debug("REST request to get a page of CategoryTrees");
        Page<CategoryTree> page = categoryTreeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /category-trees/:id} : get the "id" categoryTree.
     *
     * @param id the id of the categoryTree to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the categoryTree, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/category-trees/{id}")
    public ResponseEntity<CategoryTree> getCategoryTree(@PathVariable Long id) {
        log.debug("REST request to get CategoryTree : {}", id);
        Optional<CategoryTree> categoryTree = categoryTreeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(categoryTree);
    }

    /**
     * {@code DELETE  /category-trees/:id} : delete the "id" categoryTree.
     *
     * @param id the id of the categoryTree to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/category-trees/{id}")
    public ResponseEntity<Void> deleteCategoryTree(@PathVariable Long id) {
        log.debug("REST request to delete CategoryTree : {}", id);
        categoryTreeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString())).build();
    }
}