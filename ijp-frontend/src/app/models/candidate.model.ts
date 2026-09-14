export interface Candidate {
  id?: number;
  firstName: string;
  lastName: string;
  employeeId: string;
  dob: string;
  email: string;
  password?: string;
  role?: string;
  jobId: number;
  status?: string;
}
