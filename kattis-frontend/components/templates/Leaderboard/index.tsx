"use client";

import { useLeaderboard } from "./index.hooks";
import LoadingErrorAlert from "./loading-error-alert";
import LoadingSpinner from "./loading-spinner";
import UniversityChart from "./university-chart";

export const LeaderboardTemplate = () => {
  const { universityData, isLoading, isError } = useLeaderboard();

  return (
    <div className="p-4 flex gap-4 flex-col">
      <h1 className="text-3xl font-bold">Leaderboard</h1>
      {isError && <LoadingErrorAlert />}
      {isLoading && <LoadingSpinner />}
      {universityData && <UniversityChart data={universityData} />}
    </div>
  );
};
