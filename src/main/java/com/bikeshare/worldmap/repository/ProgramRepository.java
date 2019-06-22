package com.bikeshare.worldmap.repository;

import com.bikeshare.worldmap.model.Program;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends CrudRepository<Program, Long> {
    @Override
    @RestResource(exported = false)
    <S extends Program> S save(S entity);

    @Override
    @RestResource(exported = false)
    <S extends Program> Iterable<S> saveAll(Iterable<S> entities);

    @Override
    @RestResource(exported = false)
    void deleteById(Long aLong);

    @Override
    @RestResource(exported = false)
    void delete(Program entity);

    @Override
    @RestResource(exported = false)
    void deleteAll(Iterable<? extends Program> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();
}
