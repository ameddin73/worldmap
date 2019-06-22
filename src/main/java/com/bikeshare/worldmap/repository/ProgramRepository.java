package com.bikeshare.worldmap.repository;

import com.bikeshare.worldmap.model.Program;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends CrudRepository<Program, Long> {
}
