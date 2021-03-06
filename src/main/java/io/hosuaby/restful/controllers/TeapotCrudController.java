package io.hosuaby.restful.controllers;

import io.hosuaby.restful.domain.Teapot;
import io.hosuaby.restful.mappers.TeapotMapper;
import io.hosuaby.restful.mappings.TeapotMapping;
import io.hosuaby.restful.services.TeapotCrudService;
import io.hosuaby.restful.services.exceptions.teapots.TeapotAlreadyExistsException;
import io.hosuaby.restful.services.exceptions.teapots.TeapotNotExistsException;
import io.hosuaby.restful.services.exceptions.teapots.TeapotsNotExistException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for CRUD operations of teapots.
 */
@RestController
@RequestMapping("/crud/teapots")
public class TeapotCrudController {

    /** Default teapots to fill repository */
    private static final Set<Teapot> TEAPOTS = new HashSet<Teapot>() {
        private static final long serialVersionUID = 1L;
        {
            add(new Teapot("mouse", "Mouse", "Tefal", Teapot.L0_3));
            add(new Teapot("einstein", "Einstein", "Sony", Teapot.L3));
            add(new Teapot("nemezis", "Nemezis", "Philips", Teapot.L10));
        }
    };

    /** Teapot CRUD service */
    @Autowired
    private TeapotCrudService crud;

    /** Teapot mapper */
    @Autowired
    private TeapotMapper mapper;

    /**
     * Returns all existing teapots. If ids parameter is defined returns teapots
     * found by their ids.
     *
     * @param ids    teapot ids (optional)
     *
     * @return collection of teapots
     *
     * @throws TeapotsNotExistException
     *      when any of defined teapots was not found
     */
    @RequestMapping(
            value = "/",
            method = RequestMethod.GET)
    @Mapped
    public Collection<Teapot> findAll(
            @RequestParam(required = false) String[] ids)
                    throws TeapotsNotExistException {
        if (ids != null) {
            return crud.findAll(ids);
        } else {
            return crud.findAll();
        }
    }

    /**
     * Returns teapot found by id.
     *
     * @param id    teapot id
     *
     * @return found teapot
     *
     * @throws TeapotNotExistsException
     *      when teapot was not found
     */
    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.GET)
    @Mapped
    public Teapot find(@PathVariable String id)
            throws TeapotNotExistsException {
        return crud.find(id);
    }

    /**
     * @return count of existing teapots.
     */
    @RequestMapping(
            value = "/count",
            method = RequestMethod.GET)
    public long count() {
        return crud.count();
    }

    /**
     * Deletes teapot by id.
     *
     * @param id    teapot id.
     *
     * @throws TeapotNotExistsException
     *      when teapot was not found
     */
    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id)
            throws TeapotNotExistsException {
        crud.delete(id);
    }

    /**
     * Adds a new teapot.
     *
     * @param teapot    new teapot
     *
     * @throws TeapotAlreadyExistsException
     *      when teapot with same id already exists
     */
    @RequestMapping(
            value = "/",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public void add(@RequestBody @Mapped @Valid Teapot teapot)
            throws TeapotAlreadyExistsException {
        crud.add(teapot);
    };

    /**
     * Replaces teapot found by id by a new one.
     *
     * @param id               id of the teapot to replace
     * @param updatedTeapot    teapot mapping
     *
     * @throws TeapotNotExistsException
     *      when teapot to replace was not found
     * @throws TeapotAlreadyExistsException
     *      when new teapot has another id than replaced one and the same id
     *      with already existing teapot
     */
    @RequestMapping(
            value = "/{id}",
            method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // TODO: This method has too much boilerplate. Think how to make update
    //       from mapping object more elegant.
    // TODO: do not use put when transfering mappings between client and server.
    //       use patch instead
    public void update(
            @PathVariable String id,
            @RequestBody @Mapped @Valid Teapot updatedTeapot)
                    throws TeapotNotExistsException,
                        TeapotAlreadyExistsException {

        /* Transform teapot back to mapping */
        // TODO: sorry it's really ugly
        TeapotMapping mapping = mapper.toMapping(updatedTeapot);

        Teapot oldTeapot = crud.find(id);

        /* Update old teapot */
        mapper.fromMapping(mapping, oldTeapot);

        /* Save teapot */
        crud.update(id, oldTeapot);
    }

    /**
     * Resets the teapot repository into initial state.
     */
    @RequestMapping(
            value = "/reset",
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostConstruct
    public synchronized void reset() {
        try {
            crud.deleteAll();
            for (Teapot teapot : TEAPOTS) {
                if (crud.exists(teapot)) {
                    crud.update(teapot.getId(), teapot);
                } else {
                    crud.add(teapot);
                }
            }
        } catch (TeapotNotExistsException | TeapotAlreadyExistsException e) {
            e.printStackTrace();
        }
    }

}
