import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { JobService } from '../../services/job.service';
import { AuthService } from '../../services/auth.service';
import { JobPosting } from '../../models/job.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  openJobs: JobPosting[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private jobService: JobService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.fetchOpenJobs();
  }

  fetchOpenJobs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.jobService.getOpenJobs().subscribe({
      next: (jobs) => {
        this.openJobs = jobs;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load open jobs. Please ensure backend microservices are running.';
        this.isLoading = false;
        console.error('Error fetching open jobs:', err);
      }
    });
  }

  onApplyJob(job: JobPosting): void {
    if (this.authService.isAdmin()) {
      alert('Administrators and HR Admins are not allowed to apply for job postings.');
      return;
    }
    const targetUrl = `/apply?jobId=${job.id}&code=${encodeURIComponent(job.jobId)}&title=${encodeURIComponent(job.designation)}`;
    
    if (this.authService.isLoggedIn() && this.authService.isEmployee()) {
      this.router.navigateByUrl(targetUrl);
    } else {
      // Prompt employee login and preserve returnUrl to selected job
      this.router.navigate(['/login'], { queryParams: { returnUrl: targetUrl } });
    }
  }

  closeJob(id: number): void {
    if (confirm('Are you sure you want to close this job posting? Candidates will no longer be able to apply.')) {
      this.jobService.closeJob(id).subscribe({
        next: () => this.fetchOpenJobs(),
        error: (err) => alert('Failed to close job.')
      });
    }
  }

  scrollToPositions(): void {
    const element = document.getElementById('open-positions');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }
}
