import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  // Check if we are in the browser and have a token

  if (typeof window !== 'undefined' && window.localStorage) {
    const token = localStorage.getItem('jwt_token');

    // If we have a token, clone the request and attach it

    if (token) {
      const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });

      return next(authReq);
    }
  }

  // If no token, just send the request as is

  return next(req);
};
