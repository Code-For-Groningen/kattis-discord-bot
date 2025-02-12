import { Alert } from "@/components/ui/alert";

const LoadingErrorAlert = () => {
  return (
    <Alert variant="destructive">
      <p>There was an error loading the leaderboard. Please try again later.</p>
    </Alert>
  );
};

export default LoadingErrorAlert;
