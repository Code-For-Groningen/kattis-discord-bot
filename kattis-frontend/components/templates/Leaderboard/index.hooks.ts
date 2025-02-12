import { fetchWithAuth } from "@/lib/httpCalls";
import { useQuery } from "@tanstack/react-query";

// export const getMockUniversityData = (): University => {
//   let names = ["Alice", "Bob", "Charlie", "David", "Eve"];
//   let players: Player[] = [];

//   const dates = Array.from(
//     { length: 10 },
//     (_, i) => new Date().getTime() - i * 1000 * 60 * 60 * 24,
//   );

//   for (let i = 0; i < 5; i++) {
//     const history: PlayerScoreHistory[] = dates.map((date) => ({
//       date: date,
//       score: Math.floor(Math.random() * 100),
//     }));

//     players.push({
//       playerName: names[i],
//       profilePictureUrl: "",
//       scoreHistory: history,
//       currentScore: history[history.length - 1].score,
//     });
//   }

//   // players = players.sort((a, b) => b.currentScore - a.currentScore);

//   const exampleData: University = {
//     universityName: "University of Groningen",
//     universityRank: Math.floor(Math.random() * 100),
//     universityScore: Math.floor(Math.random() * 100),
//     players: players,
//   };

//   return exampleData;
// };

export const getUniversityData = async (): Promise<University> => {
  return fetch(`/api`)
          .then((data) => data.json())
};


export const useUniversityDataQuery = () => {
  // const url = `${process.env.NEXT_PUBLIC_API_URL}/api/university`;

  // return useQuery({
  //     queryKey: ["useUniversityDataQuery"],
  //     queryFn: async () => fetchWithAuth<UniversityData>(url),
  //     refetchInterval: 1000 * 10, // 10 seconds
  // });

  return useQuery({
    queryKey: ["useUniversityDataQuery"],
    queryFn: getUniversityData,
    refetchInterval: 1000 * 10, // 10 seconds
  });
};

export const useLeaderboard = () => {
  const universityDataQuery = useUniversityDataQuery();

  return {
    universityData: universityDataQuery.data,
    isLoading: universityDataQuery.isLoading,
    isError: universityDataQuery.isError,
  };
};
