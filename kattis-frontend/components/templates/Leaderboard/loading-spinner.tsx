import { Loader2 } from "lucide-react";

const LoadingSpinner = () => {
  return (
    <div className="w-full flex justify-center">
      <Loader2 className="w-10 h-10 text-blue-500 animate-spin" />
    </div>
  );
};

export default LoadingSpinner;
