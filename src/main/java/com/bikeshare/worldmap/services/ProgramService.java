package com.bikeshare.worldmap.services;

import com.bikeshare.worldmap.model.Program;
import com.bikeshare.worldmap.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ProgramService {
    private ProgramRepository programRepository;

    @Autowired
    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public Program createProgram(Long id, String city, String continent, String country, Date endDate, Float latitude, Float longitude, String name, Date startDate, Integer status, String url) {
        if (!programRepository.existsById(id)) {
            return programRepository.save(new Program(city, continent, country, endDate, latitude, longitude, name, startDate, status, url));
        }
        return null;
    }

    public Iterable<Program> lookup() {
        return programRepository.findAll();
    }

    public long total() {
        return programRepository.count();
    }
}
