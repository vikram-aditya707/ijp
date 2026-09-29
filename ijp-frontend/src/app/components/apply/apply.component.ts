import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CandidateService } from '../../services/candidate.service';
import { AuthService } from '../../services/auth.service';
import { Candidate } from '../../models/candidate.model';

@Component({
  selector: 'app-apply',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './apply.component.html',
  styleUrls: ['./apply.component.css']
})
export class ApplyComponent implements OnInit {

  candidate: Candidate = {
    firstName: '',
    lastName: '',
    employeeId: '',
    dob: '',
    email: '',
    password: '',
    jobId: 0
  };

  selectedJobCode = '';
  selectedJobTitle = '';

  // Email Typeahead Suggestions for authenticated employee
  emailSuggestions: string[] = [];
  filteredEmailSuggestions: string[] = [];
  showEmailDropdown = false;
  selectedEmailIndex = -1;

  isSubmitting = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private candidateService: CandidateService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    if (this.authService.isAdmin()) {
      alert('Administrators and HR Admins are not allowed to apply for job postings.');
      this.router.navigate(['/admin-dashboard']);
      return;
    }

    const currentUser = this.authService.currentUserValue;
    if (currentUser && currentUser.role !== 'ADMIN') {
      const nameParts = currentUser.name.split(' ');
      this.candidate.firstName = nameParts[0] || '';
      this.candidate.lastName = nameParts.slice(1).join(' ') || '';
      this.candidate.email = currentUser.email || '';
      this.candidate.employeeId = currentUser.employeeId || '';
      if (currentUser.email) {
        this.emailSuggestions = [currentUser.email];
      }
    }

    this.route.queryParams.subscribe(params => {
      if (params['jobId']) {
        this.candidate.jobId = +params['jobId'];
      }
      if (params['code']) {
        this.selectedJobCode = params['code'];
      }
      if (params['title']) {
        this.selectedJobTitle = params['title'];
      }
    });
  }

  get minDobDate(): string {
    const today = new Date();
    const minDate = new Date(today.getFullYear() - 80, today.getMonth(), today.getDate());
    return minDate.toISOString().split('T')[0];
  }

  get maxDobDate(): string {
    const today = new Date();
    const maxDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    return maxDate.toISOString().split('T')[0];
  }

  // Email Typeahead Handlers
  onEmailInput(): void {
    this.selectedEmailIndex = -1;
    const val = (this.candidate.email || '').trim().toLowerCase();
    if (!val || this.emailSuggestions.length === 0) {
      this.filteredEmailSuggestions = [];
      this.showEmailDropdown = false;
      return;
    }

    this.filteredEmailSuggestions = this.emailSuggestions.filter(e => e.toLowerCase().includes(val));
    this.showEmailDropdown = this.filteredEmailSuggestions.length > 0;
  }

  selectEmail(email: string): void {
    this.candidate.email = email;
    this.showEmailDropdown = false;
    this.filteredEmailSuggestions = [];
    this.selectedEmailIndex = -1;
  }

  onEmailKeyDown(event: KeyboardEvent): void {
    if (!this.showEmailDropdown || this.filteredEmailSuggestions.length === 0) return;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedEmailIndex = (this.selectedEmailIndex + 1) % this.filteredEmailSuggestions.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedEmailIndex = (this.selectedEmailIndex - 1 + this.filteredEmailSuggestions.length) % this.filteredEmailSuggestions.length;
    } else if (event.key === 'Enter') {
      if (this.selectedEmailIndex >= 0 && this.selectedEmailIndex < this.filteredEmailSuggestions.length) {
        event.preventDefault();
        this.selectEmail(this.filteredEmailSuggestions[this.selectedEmailIndex]);
      }
    } else if (event.key === 'Escape') {
      this.showEmailDropdown = false;
      this.selectedEmailIndex = -1;
    }
  }

  onEmailBlur(): void {
    setTimeout(() => {
      this.showEmailDropdown = false;
    }, 200);
  }

  // Validation Helpers
  hasFirstNameDigits(): boolean {
    return /\d/.test(this.candidate.firstName || '');
  }

  isFirstNameValid(): boolean {
    const fn = (this.candidate.firstName || '').trim();
    if (!fn) return false;
    return !this.hasFirstNameDigits();
  }

  hasLastNameDigits(): boolean {
    return /\d/.test(this.candidate.lastName || '');
  }

  isLastNameValid(): boolean {
    const ln = (this.candidate.lastName || '').trim();
    if (!ln) return true; // Optional!
    return !this.hasLastNameDigits();
  }

  isEmployeeIdValid(): boolean {
    return !!(this.candidate.employeeId || '').trim();
  }

  isAgeValid(): boolean {
    if (!this.candidate.dob) return false;
    const dobDate = new Date(this.candidate.dob);
    const today = new Date();
    if (isNaN(dobDate.getTime()) || dobDate > today) return false;
    let age = today.getFullYear() - dobDate.getFullYear();
    const m = today.getMonth() - dobDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dobDate.getDate())) {
      age--;
    }
    return age >= 18 && age <= 80;
  }

  isEmailValid(): boolean {
    const em = (this.candidate.email || '').trim().toLowerCase();
    if (!em) return false;
    return /^[^@\s]+@company\.com$/.test(em);
  }

  isJobIdValid(): boolean {
    return this.candidate.jobId !== null && this.candidate.jobId !== undefined && this.candidate.jobId > 0;
  }

  isFormValid(): boolean {
    return this.isJobIdValid() &&
           this.isFirstNameValid() &&
           this.isLastNameValid() &&
           this.isEmployeeIdValid() &&
           this.isAgeValid() &&
           this.isEmailValid();
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (this.authService.isAdmin()) {
      this.errorMessage = 'Administrators and HR Admins are not allowed to apply for job postings.';
      return;
    }

    if (!this.isFormValid()) {
      this.errorMessage = 'Please fix all validation errors before submitting application.';
      return;
    }

    this.isSubmitting = true;

    this.candidateService.applyForJob(this.candidate).subscribe({
      next: (response) => {
        const successMsg = `Application submitted successfully for candidate ${response.firstName} ${response.lastName || ''}!`;
        this.successMessage = successMsg;
        setTimeout(() => {
          this.router.navigate(['/'], { queryParams: { applied: 'true', message: successMsg } });
        }, 1500);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Failed to submit application. Please verify details and try again.';
        console.error('Application error:', err);
      }
    });
  }
}
