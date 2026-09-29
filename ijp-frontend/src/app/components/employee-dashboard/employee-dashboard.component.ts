import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService, UserSession } from '../../services/auth.service';
import { CandidateService } from '../../services/candidate.service';
import { Candidate } from '../../models/candidate.model';
import { Interview } from '../../models/interview.model';

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {

  currentUser: UserSession | null = null;
  myApplications: Candidate[] = [];
  myInterviews: Interview[] = [];
  unreadNotifCount = 0;
  isLoading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private candidateService: CandidateService) {}

  ngOnInit(): void {
    this.currentUser = this.authService.currentUserValue;
    if (this.currentUser) {
      this.loadEmployeeData();
    }
  }

  loadEmployeeData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    if (this.currentUser && this.currentUser.email) {
      this.candidateService.getCandidatesByEmail(this.currentUser.email).subscribe({
        next: (apps) => {
          this.myApplications = (apps || []).filter(app => app.jobId && app.jobId > 0);
          this.isLoading = false;
        },
        error: (err) => {
          if (this.currentUser?.id) {
            this.candidateService.getCandidateById(this.currentUser.id).subscribe({
              next: (cand) => {
                this.myApplications = (cand && cand.jobId && cand.jobId > 0) ? [cand] : [];
                this.isLoading = false;
              },
              error: (e) => {
                this.errorMessage = 'Could not fetch employee profile.';
                this.isLoading = false;
              }
            });
          } else {
            this.isLoading = false;
          }
        }
      });
    } else if (this.currentUser && this.currentUser.id) {
      this.candidateService.getCandidateById(this.currentUser.id).subscribe({
        next: (cand) => {
          this.myApplications = [cand];
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Could not fetch employee profile.';
          this.isLoading = false;
        }
      });
    } else {
      this.isLoading = false;
    }

    if (this.currentUser && this.currentUser.id) {
      // Load interviews
      this.candidateService.getInterviewsByCandidateId(this.currentUser.id).subscribe({
        next: (interviews) => this.myInterviews = interviews,
        error: (err) => console.error(err)
      });

      // Load unread count
      this.candidateService.getUnreadNotificationCount(this.currentUser.id).subscribe({
        next: (res) => this.unreadNotifCount = res.count,
        error: (err) => console.error(err)
      });
    }
  }
}
