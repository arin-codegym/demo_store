import { FaStar, FaRegStar } from 'react-icons/fa';

function Rating({ rating }: { rating: number }) {
  /*   Giả sử rating = 3:
0 + 1 <= 3 → true
1 + 1 <= 3 → true
2 + 1 <= 3 → true
3 + 1 <= 3 → false
4 + 1 <= 3 → false */
  const stars = Array.from({ length: 5 }, (_, i) => i + 1 <= rating);

  return (
    <div className='flex items-center gap-x-1'>
      {stars.map((isFilled, i) => {
        const className = `w-3 h-3 ${
          isFilled ? 'text-primary' : 'text-gray-400'
        }`;
        return isFilled ? (
          <FaStar className={className} key={i} />
        ) : (
          <FaRegStar className={className} key={i} />
        );
      })}
    </div>
  );
}

export default Rating;
