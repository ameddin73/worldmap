import { Component, OnInit } from '@angular/core';
import {ProgramService} from "../shared/program/program.service";
import {Program} from "../models/program.model";

@Component({
  selector: 'app-program-list',
  templateUrl: './program-list.component.html',
  styleUrls: ['./program-list.component.css']
})
export class ProgramListComponent implements OnInit {
  programs: Array<Program>;

  constructor(private programService: ProgramService) { }

  ngOnInit() {
    this.programService.getAll().subscribe(data => {
      this.programs = data._embedded.programs;
    })
  }

}
