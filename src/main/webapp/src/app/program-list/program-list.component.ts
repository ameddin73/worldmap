import { Component, OnInit } from '@angular/core';
import {ProgramService} from "../shared/program/program.service";

@Component({
  selector: 'app-program-list',
  templateUrl: './program-list.component.html',
  styleUrls: ['./program-list.component.css']
})
export class ProgramListComponent implements OnInit {
  programs: Array<any>;

  constructor(private programService: ProgramService) { }

  ngOnInit() {
    this.programService.getAll().subscribe(data => {
      this.programs = data._embedded.programs;
    })
  }

}
